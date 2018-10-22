package uniregistrar.driver.did.btcr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
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

import did.DIDDocument;
import info.weboftrust.txrefconversion.Chain;
import info.weboftrust.txrefconversion.ChainAndBlockLocation;
import info.weboftrust.txrefconversion.TxrefConverter;
import info.weboftrust.txrefconversion.bitcoinconnection.BitcoinConnection;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.btcr.bitcoinconnection.BTCDRPCBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoindRPCBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniregistrar.driver.did.btcr.diddoccontinuation.DIDDocContinuation;
import uniregistrar.driver.did.btcr.diddoccontinuation.LocalFileDIDDocContinuation;
import uniregistrar.driver.did.btcr.state.RegisterStateWaitDidBtcrConfirm;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateFinished;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public class DidBtcrDriver extends AbstractDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	private Map<String, Object> properties;

	private String peerMainnet;
	private String peerTestnet;
	private DIDDocContinuation didDocContinuation;
	private BitcoinConnection bitcoinConnection;

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
			String env_bitcoinConnection = System.getenv("uniresolver_driver_did_btcr_bitcoinConnection");
			String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlMainnet");
			String env_rpcUrlTestnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlTestnet");

			if (env_peerMainnet != null) properties.put("peerMainnet", env_peerMainnet);
			if (env_peerTestnet != null) properties.put("peerTestnet", env_peerTestnet);
			if (env_didDocContinuation != null) properties.put("didDocContinuation", env_didDocContinuation);
			if (env_basePath != null) properties.put("basePath", env_basePath);
			if (env_baseUri != null) properties.put("baseUri", env_baseUri);
			if (env_bitcoinConnection != null) properties.put("bitcoinConnection", env_bitcoinConnection);
			if (env_rpcUrlMainnet != null) properties.put("rpcUrlMainnet", env_rpcUrlMainnet);
			if (env_rpcUrlTestnet != null) properties.put("rpcUrlTestnet", env_rpcUrlTestnet);
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
			String prop_bitcoinConnection = (String) this.getProperties().get("bitcoinConnection");

			if (prop_peerMainnet != null) this.setPeerMainnet(prop_peerMainnet);
			if (prop_peerTestnet != null) this.setPeerTestnet(prop_peerTestnet);

			if ("localfile".equals(prop_didDocContinuation)) {

				this.setDIDDocContinuation(new LocalFileDIDDocContinuation());

				String prop_basePath = (String) this.getProperties().get("basePath");
				String prop_baseUri = (String) this.getProperties().get("baseUri");

				if (prop_basePath != null) ((LocalFileDIDDocContinuation) this.getDIDDocContinuation()).setBasePath(prop_basePath);
				if (prop_baseUri != null) ((LocalFileDIDDocContinuation) this.getDIDDocContinuation()).setBaseUri(prop_baseUri);
			}

			if ("bitcoind".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BitcoindRPCBitcoinConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlTestnet = (String) this.getProperties().get("rpcUrlTestnet");

				if (prop_rpcUrlMainnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlTestnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlTestnet(prop_rpcUrlTestnet);
			} else if ("btcd".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BTCDRPCBitcoinConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlTestnet = (String) this.getProperties().get("rpcUrlTestnet");

				if (prop_rpcUrlMainnet != null) ((BTCDRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlTestnet != null) ((BTCDRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlTestnet(prop_rpcUrlTestnet);
			} else if ("bitcoinj".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BitcoinjSPVBitcoinConnection());
			} else  if ("blockcypherapi".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BlockcypherAPIBitcoinConnection());
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// open wallet app kits

		if (this.getWalletAppKitMainnet() == null || this.getWalletAppKitTestnet() == null ) this.openWalletAppKits();

		// CONTINUE JOB?

		String jobId = registerRequest.getJobId();

		if (jobId != null) {

			DidBtcrJob job = (DidBtcrJob) this.continueJob(jobId);
			if (job == null) throw new RegistrationException("Invalid job: " + jobId);

			return this.continueRegisterJob(jobId, job, registerRequest.getOptions());
		}

		// read options

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

		double balance = ((double) walletAppKit.wallet().getBalance().getValue() / Coin.COIN.getValue());
		Address changeAddress = walletAppKit.wallet().currentChangeAddress();

		if (log.isDebugEnabled()) log.debug("Balance: " + balance);
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

		Transaction transaction;
		String transactionHash;

		try {

			transaction = future.get();
			transactionHash = transaction.getHashAsString();
		} catch (InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot sent transaction: " + ex.getMessage());
		}

		if (log.isDebugEnabled()) log.debug("Sent transaction! Transaction hash is " + transactionHash);
		if (log.isDebugEnabled()) for (TransactionInput input : transaction.getInputs()) log.debug("Transaction input: " + input.getValue() + " " + input); 
		if (log.isDebugEnabled()) for (TransactionOutput output : transaction.getOutputs()) log.debug("Transaction output: " + output.getValue() + " " + output); 

		// determine private key

		String privateKeyAsWif = null;
		String privateKeyAsHex = null;

		KeyBag decryptingKeyBag = new DecryptingKeyBag(walletAppKit.wallet(), sendRequest.aesKey);
		for (TransactionInput input : transaction.getInputs()) {

			RedeemData redeemData = input.getConnectedRedeemData(decryptingKeyBag);
			privateKeyAsWif = redeemData.keys.get(0).getPrivateKeyAsWiF(walletAppKit.params());
			privateKeyAsHex = redeemData.keys.get(0).getPrivateKeyAsHex();
		}

		// REGISTER STATE: wait

		DidBtcrJob job = new DidBtcrJob(chain, transactionHash, didContinuationUri, privateKeyAsWif, privateKeyAsHex);
		jobId = this.newJob(job);

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("chain", chain);
		methodMetadata.put("transactionHash", transactionHash);
		methodMetadata.put("balance", Double.valueOf(balance));
		methodMetadata.put("changeAddress", changeAddress.toString());

		RegisterState registerState = new RegisterStateWaitDidBtcrConfirm(jobId, null, methodMetadata);
		return registerState;
	}

	private RegisterState continueRegisterJob(String jobId, DidBtcrJob job, Map<String, Object> options) throws RegistrationException {

		// read job

		String chain = job.getChain();
		String transactionHash = job.getTransactionHash();
		URI didContinuationUri = job.getDidContinuationUri();
		String privateKeyAsWif = job.getPrivateKeyAsWif();
		String privateKeyAsHex = job.getPrivateKeyAsHex();

		// read options

		// determine txref

		String txref;

		try {

			ChainAndBlockLocation chainAndBlockLocation = this.getBitcoinConnection().getChainAndBlockLocation(Chain.valueOf(chain), transactionHash);
			txref = TxrefConverter.get().txrefEncode(chainAndBlockLocation);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot determine txref: " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Determined txref: " + txref);

		// REGISTER STATE: wait

		if (txref == null) {

			Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
			methodMetadata.put("chain", chain);
			methodMetadata.put("transactionHash", transactionHash);

			RegisterState registerState = new RegisterStateWaitDidBtcrConfirm(jobId, null, methodMetadata);
			return registerState;
		}

		// store continuation DID Document

		DIDDocument didContinuationDocument = DIDDocument.build("did:btcr:" + txref, null, null, null, null);

		try {

			this.didDocContinuation.storeDIDDocContinuation(didContinuationUri, didContinuationDocument);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot store continuation DID Document: " + ex.getMessage(), ex);
		}

		// REGISTRATION STATE: finished

		this.finishJob(jobId);

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("chain", chain);
		methodMetadata.put("transactionHash", transactionHash);

		String identifier = "did:btcr:" + txref;

		Map<String, Object> secret = new LinkedHashMap<String, Object> ();
		secret.put("privateKeyWif", privateKeyAsWif);
		secret.put("privateKeyHex", privateKeyAsHex);

		RegisterState registerState = new RegisterStateFinished(null, null, methodMetadata, identifier, secret);
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
		//		URI uriMainnet = URI.create("temp://" + this.getPeerMainnet());
		//		URI uriTestnet = URI.create("temp://" + this.getPeerTestnet());

		this.walletAppKitMainnet = new WalletAppKit(mainnetParams, new File("./wallet/"), "mainNetWallet");
		//		this.walletAppKitMainnet.setPeerNodes((new PeerAddress(mainnetParams, uriMainnet.getHost(), uriMainnet.getPort())));
		if (log.isInfoEnabled()) log.info("Opened mainnet wallet app kit: " + this.getPeerMainnet());

		this.walletAppKitTestnet = new WalletAppKit(testnetParams, new File("./wallet/"), "testNetWallet");
		//		this.walletAppKitTestnet.setPeerNodes((new PeerAddress(testnetParams, uriTestnet.getHost(), uriTestnet.getPort())));
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

	private static class DidBtcrJob implements AbstractDriver.Job {

		private String chain;
		private String transactionHash;
		private URI didContinuationUri;
		private String privateKeyAsWif;
		private String privateKeyAsHex;

		private DidBtcrJob(String chain, String transactionHash, URI didContinuationUri, String privateKeyAsWif, String privateKeyAsHex) {

			this.chain = chain;
			this.transactionHash = transactionHash;
			this.didContinuationUri = didContinuationUri;
			this.privateKeyAsWif = privateKeyAsWif;
			this.privateKeyAsHex = privateKeyAsHex;
		}

		private String getChain() {

			return this.chain;
		}

		private String getTransactionHash() {

			return this.transactionHash;
		}

		private URI getDidContinuationUri() {

			return this.didContinuationUri;
		}

		private String getPrivateKeyAsWif() {

			return this.privateKeyAsWif;
		}

		private String getPrivateKeyAsHex() {

			return this.privateKeyAsHex;
		}
	}

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

	public BitcoinConnection getBitcoinConnection() {

		return this.bitcoinConnection;
	}

	public void setBitcoinConnection(BitcoinConnection bitcoinConnection) {

		this.bitcoinConnection = bitcoinConnection;
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
