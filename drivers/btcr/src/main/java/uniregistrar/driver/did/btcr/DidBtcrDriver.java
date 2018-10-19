package uniregistrar.driver.did.btcr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.wallet.DecryptingKeyBag;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.SendRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.orchid.encoders.Hex;

import did.DIDDocument;
import info.weboftrust.txrefconversion.Chain;
import info.weboftrust.txrefconversion.TxrefConverter;
import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.btcr.diddoccontinuation.DIDDocContinuation;
import uniregistrar.driver.did.btcr.diddoccontinuation.LocalFileDIDDocContinuation;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateFinished;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	private Map<String, Object> properties;

	private String peerMainnet;
	private String peerTestnet;
	private DIDDocContinuation didDocContinuation;

	private WalletAppKit walletAppKitMainnet = null;
	private WalletAppKit walletAppKitTestnet = null;

	public DidBtcrDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidBtcrDriver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_peerMainnet = System.getenv("uniregistrar_driver_did_btcr_peerMainnet");
			String env_peerTestnet = System.getenv("uniregistrar_driver_did_btcr_peerTestnet");
			String env_didDocContinuation = System.getenv("uniregistrar_driver_did_btcr_didDocContinuation");
			String env_basePath = System.getenv("uniregistrar_driver_did_btcr_basePath");
			String env_baseUri = System.getenv("uniregistrar_driver_did_btcr_baseUri");

			if (env_peerMainnet != null) properties.put("peerMainnet", env_peerMainnet);
			if (env_peerTestnet != null) properties.put("peerTestnet", env_peerTestnet);
			if (env_didDocContinuation != null) properties.put("didDocContinuation", env_didDocContinuation);
			if (env_basePath != null) properties.put("basePath", env_basePath);
			if (env_baseUri != null) properties.put("baseUri", env_baseUri);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_peerMainnet = (String) this.getProperties().get("peerMainnet");
			String prop_peerTestnet = (String) this.getProperties().get("peerTestnet");
			String prop_didDocContinuation = (String) this.getProperties().get("didDocContinuation");

			if (prop_peerMainnet != null) this.setPeerMainnet(prop_peerMainnet);
			if (prop_peerTestnet != null) this.setPeerTestnet(prop_peerTestnet);

			if ("localfile".equals(prop_didDocContinuation)) {

				this.setDIDDocContinuation(new LocalFileDIDDocContinuation());

				String prop_basePath = (String) this.getProperties().get("basePath");
				String prop_baseUri = (String) this.getProperties().get("baseUri");

				if (prop_basePath != null) ((LocalFileDIDDocContinuation) this.getDIDDocContinuation()).setBasePath(prop_basePath);
				if (prop_baseUri != null) ((LocalFileDIDDocContinuation) this.getDIDDocContinuation()).setBaseUri(prop_baseUri);
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// open wallet app kits

		if (this.getWalletAppKitMainnet() == null || this.getWalletAppKitTestnet() == null ) this.openWalletAppKits();

		// read parameters

		String chain = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("chain");
		if (chain == null || chain.trim().isEmpty()) chain = "TESTNET";

		// find wallet app kit

		WalletAppKit walletAppKit = null;
		if ("MAINNET".equals(chain)) walletAppKit = this.getWalletAppKitMainnet();
		if ("TESTNET".equals(chain)) walletAppKit = this.getWalletAppKitTestnet();
		if (walletAppKit == null) throw new RegistrationException("Unknown network: " + chain);

		// create continuation DID Document

		URI didContinuationUri = this.didDocContinuation.prepareDIDDocContinuation(null);

		// prepare transaction

		if (log.isDebugEnabled()) log.debug("Balance: " + ((double) walletAppKit.wallet().getBalance().getValue() / Coin.COIN.getValue()));
		if (log.isDebugEnabled()) log.debug("Change address: " + walletAppKit.wallet().currentChangeAddress());

		Transaction originalTransaction = new Transaction(walletAppKit.params());
		originalTransaction.addOutput(Coin.ZERO, new URIScriptBuilder(didContinuationUri).build());
		SendRequest sendRequest = SendRequest.forTx(originalTransaction);

		try {

			walletAppKit.wallet().completeTx(sendRequest);
		} catch (InsufficientMoneyException ex) {

			throw new RegistrationException("Insufficent coins: " + ex.getMessage());
		}

		// send transaction

		TransactionBroadcast transactionBroadcast = walletAppKit.peerGroup().broadcastTransaction(originalTransaction);
		ListenableFuture<Transaction> future = transactionBroadcast.future();

		Transaction sentTransaction;

		try {

			sentTransaction = future.get();
		} catch (InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot sent transaction: " + ex.getMessage());
		}

		if (log.isDebugEnabled()) log.debug("Sent transaction! Transaction hash is " + sentTransaction.getHashAsString());
		if (log.isDebugEnabled()) for (TransactionInput input : sentTransaction.getInputs()) log.debug("Transaction input: " + input.getValue() + " " + input); 
		if (log.isDebugEnabled()) for (TransactionOutput output : sentTransaction.getOutputs()) log.debug("Transaction output: " + output.getValue() + " " + output); 

		// determine txref

		String txref;

		try {

			txref = TxrefConverter.get().txidToTxref(sentTransaction.getHashAsString(), Chain.valueOf(chain));
		} catch (IOException ex) {

			throw new RegistrationException("Cannot determine txref: " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Determined txref: " + txref);

		// determine private key

		KeyBag maybeDecryptingKeyBag = new DecryptingKeyBag(walletAppKit.wallet(), sendRequest.aesKey);
		for (TransactionInput input : sentTransaction.getInputs()) {

			RedeemData redeemData = input.getConnectedRedeemData(maybeDecryptingKeyBag);
			if (log.isDebugEnabled()) log.debug("redeem: " + input + " --> " + redeemData.keys.get(0).getPrivateKeyAsHex());
			if (log.isDebugEnabled()) log.debug("redeem: " + input + " --> " + redeemData.keys.get(0).getPrivateKeyAsWiF(walletAppKit.params()));
			if (log.isDebugEnabled()) log.debug("redeem: " + input + " --> " + redeemData.keys.get(0).getPrivateKeyEncoded(walletAppKit.params()).toBase58());
			if (log.isDebugEnabled()) log.debug("redeem: " + input + " --> " + redeemData.keys.get(0).getPrivateKeyEncoded(walletAppKit.params()).toString());
			if (log.isDebugEnabled()) log.debug("redeem: " + input + " --> " + new String(Hex.encode(redeemData.keys.get(0).getPrivKeyBytes())));
		}

		// store continuation DID Document

		DIDDocument didContinuationDocument = DIDDocument.build("did:btcr:" + txref, null, null, null, null);

		try {

			this.didDocContinuation.storeDIDDocContinuation(didContinuationUri, didContinuationDocument);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot store continuation DID Document: " + ex.getMessage(), ex);
		}

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("chain", chain);
		methodMetadata.put("transactionHash", sentTransaction.getHashAsString());

		// create IDENTIFIER

		String identifier = "did:btcr:";
		identifier += txref;

		// create CREDENTIALS

		Map<String, Object> credentials = new LinkedHashMap<String, Object> ();
		credentials.put("seed", "blabla");

		// create REGISTER STATE

		RegisterState registerState = new RegisterStateFinished(null, null, methodMetadata, identifier, credentials);

		// done

		return registerState;
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public RevokeState revoke(RevokeRequest revokeRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	private void openWalletAppKits() throws RegistrationException {

		// open wallet app kits

		NetworkParameters mainnetParams = MainNetParams.get();
		NetworkParameters testnetParams = TestNet3Params.get();
		URI uriMainnet = URI.create("temp://" + this.getPeerMainnet());
		URI uriTestnet = URI.create("temp://" + this.getPeerTestnet());

		this.walletAppKitMainnet = new WalletAppKit(mainnetParams, new File("."), "mainNetWallet");
		this.walletAppKitMainnet.setPeerNodes((new PeerAddress(mainnetParams, uriMainnet.getHost(), uriMainnet.getPort())));
		if (log.isInfoEnabled()) log.info("Opened mainnet wallet app kit: " + this.getPeerMainnet());

		this.walletAppKitTestnet = new WalletAppKit(testnetParams, new File("."), "testNetWallet");
		this.walletAppKitTestnet.setPeerNodes((new PeerAddress(testnetParams, uriTestnet.getHost(), uriTestnet.getPort())));
		if (log.isInfoEnabled()) log.info("Opened testnet wallet app kit: " + this.getPeerTestnet());

		// connect

		this.walletAppKitMainnet.startAsync();
		this.walletAppKitTestnet.startAsync();
		this.walletAppKitMainnet.awaitRunning();
		this.walletAppKitTestnet.awaitRunning();

		if (log.isInfoEnabled()) log.info("Connected mainnet wallet app kit: " + this.getPeerMainnet());
		if (log.isInfoEnabled()) log.info("Connected testnet wallet app kit: " + this.getPeerTestnet());
	}

	/*
	 * Helper classes
	 */

	private static class URIScriptBuilder extends ScriptBuilder {

		private URI uri;

		private URIScriptBuilder(URI uri) {

			super();
			this.uri = uri;
		}

		@Override
		public Script build() {

			this.op(ScriptOpCodes.OP_RETURN);
			this.data(this.uri.toString().getBytes(StandardCharsets.UTF_8));

			return super.build();
		}
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {

		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {

		this.properties = properties;
		this.configureFromProperties();
	}

	public String getPeerMainnet() {

		return this.peerMainnet;
	}

	public void setPeerMainnet(String peerMainnet) {

		this.peerMainnet = peerMainnet;
	}

	public String getPeerTestnet() {

		return this.peerTestnet;
	}

	public void setPeerTestnet(String peerTestnet) {

		this.peerTestnet = peerTestnet;
	}

	public DIDDocContinuation getDIDDocContinuation() {

		return this.didDocContinuation;
	}

	public void setDIDDocContinuation(DIDDocContinuation didDocContinuation) {

		this.didDocContinuation = didDocContinuation;
	}

	public WalletAppKit getWalletAppKitMainnet() {

		return this.walletAppKitMainnet;
	}

	public void setWalletAppKitMainnet(WalletAppKit walletAppKitMainnet) {

		this.walletAppKitMainnet = walletAppKitMainnet;
	}

	public WalletAppKit getWalletAppKitTestnet() {

		return this.walletAppKitTestnet;
	}

	public void setWalletAppKitTestnet(WalletAppKit walletAppKitTestnet) {

		this.walletAppKitTestnet = walletAppKitTestnet;
	}
}
