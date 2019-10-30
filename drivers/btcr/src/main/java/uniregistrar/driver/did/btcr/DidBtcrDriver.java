package uniregistrar.driver.did.btcr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
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
import org.bitcoinj.script.Script.ScriptType;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.wallet.DecryptingKeyBag;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.SendRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.bitcoinconnection.BTCDRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoindRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.btcr.diddoccontinuation.DIDDocContinuation;
import uniregistrar.driver.did.btcr.diddoccontinuation.LocalFileDIDDocContinuation;
import uniregistrar.driver.did.btcr.state.RegisterStateWaitDidBtcrConfirm;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateFinished;
import uniregistrar.state.UpdateState;

public class DidBtcrDriver extends AbstractDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	private Map<String, Object> properties;

	private String peerMainnet;
	private String peerTestnet;
	private String privateKeyMainnet;
	private String privateKeyTestnet;
	private DIDDocContinuation didDocContinuation;
	private BitcoinConnection bitcoinConnection;

	private WalletAppKit walletAppKitMainnet = null;
	private WalletAppKit walletAppKitTestnet = null;

	public DidBtcrDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidBtcrDriver() {

		this(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_peerMainnet = System.getenv("uniregistrar_driver_did_btcr_peerMainnet");
			String env_peerTestnet = System.getenv("uniregistrar_driver_did_btcr_peerTestnet");
			String env_privateKeyMainnet = System.getenv("uniregistrar_driver_did_btcr_privateKeyMainnet");
			String env_privateKeyTestnet = System.getenv("uniregistrar_driver_did_btcr_privateKeyTestnet");
			String env_didDocContinuation = System.getenv("uniregistrar_driver_did_btcr_didDocContinuation");
			String env_basePath = System.getenv("uniregistrar_driver_did_btcr_basePath");
			String env_baseUri = System.getenv("uniregistrar_driver_did_btcr_baseUri");
			String env_bitcoinConnection = System.getenv("uniregistrar_driver_did_btcr_bitcoinConnection");
			String env_rpcUrlMainnet = System.getenv("uniregistrar_driver_did_btcr_rpcUrlMainnet");
			String env_rpcUrlTestnet = System.getenv("uniregistrar_driver_did_btcr_rpcUrlTestnet");

			if (env_peerMainnet != null) properties.put("peerMainnet", env_peerMainnet);
			if (env_peerTestnet != null) properties.put("peerTestnet", env_peerTestnet);
			if (env_privateKeyMainnet != null) properties.put("privateKeyMainnet", env_privateKeyMainnet);
			if (env_privateKeyTestnet != null) properties.put("privateKeyTestnet", env_privateKeyTestnet);
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

			// parse peerMainnet, peerTestnet, privateKeyMainnet, privateKeyTestnet

			String prop_peerMainnet = (String) this.getProperties().get("peerMainnet");
			String prop_peerTestnet = (String) this.getProperties().get("peerTestnet");
			String prop_privateKeyMainnet = (String) this.getProperties().get("privateKeyMainnet");
			String prop_privateKeyTestnet = (String) this.getProperties().get("privateKeyTestnet");

			if (prop_peerMainnet != null) this.setPeerMainnet(prop_peerMainnet);
			if (prop_peerTestnet != null) this.setPeerTestnet(prop_peerTestnet);
			if (prop_privateKeyMainnet != null) this.setPrivateKeyMainnet(prop_privateKeyMainnet);
			if (prop_privateKeyTestnet != null) this.setPrivateKeyTestnet(prop_privateKeyTestnet);

			// parse bitcoinConnection

			String prop_bitcoinConnection = (String) this.getProperties().get("bitcoinConnection");

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
			} else if ("blockcypherapi".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BlockcypherAPIBitcoinConnection());
			} else {

				throw new IllegalArgumentException("Invalid bitcoinConnection: " + prop_bitcoinConnection);
			}

			// parse didDocContinuation

			String prop_didDocContinuation = (String) this.getProperties().get("didDocContinuation");

			if ("localfile".equals(prop_didDocContinuation)) {

				this.setDidDocContinuation(new LocalFileDIDDocContinuation());

				String prop_basePath = (String) this.getProperties().get("basePath");
				String prop_baseUri = (String) this.getProperties().get("baseUri");

				if (prop_basePath != null) ((LocalFileDIDDocContinuation) this.getDidDocContinuation()).setBasePath(prop_basePath);
				if (prop_baseUri != null) ((LocalFileDIDDocContinuation) this.getDidDocContinuation()).setBaseUri(prop_baseUri);
			} else {

				throw new IllegalArgumentException("Invalid didDocContinuation: " + prop_didDocContinuation);
			}
		} catch (IllegalArgumentException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// open wallets and pools

		if (this.getWalletAppKitMainnet() == null || this.getWalletAppKitTestnet() == null) this.openWalletAppKits();

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

		Context.propagate(new Context(walletAppKit.params()));

		// create continuation DID Document

		URI didContinuationUri;

		if ((registerRequest.getAddServices() != null && registerRequest.getAddServices().size() > 0) ||
				(registerRequest.getAddPublicKeys() != null && registerRequest.getAddPublicKeys().size() > 0) ||
				(registerRequest.getAddAuthentications() != null && registerRequest.getAddAuthentications().size() > 0)) {

			didContinuationUri = this.didDocContinuation.prepareDIDDocContinuation(null);
		} else {

			didContinuationUri = null;
		}

		if (log.isDebugEnabled()) log.debug("Preparing continuation DID Document: " + didContinuationUri);

		// prepare transaction

		double balance = ((double) walletAppKit.wallet().getBalance().getValue() / Coin.COIN.getValue());
		Address changeAddress = walletAppKit.wallet().currentChangeAddress();

		if (log.isDebugEnabled()) log.debug("Balance: " + balance);
		if (log.isDebugEnabled()) log.debug("Change address: " + walletAppKit.wallet().currentChangeAddress());

		Transaction originalTransaction = new Transaction(walletAppKit.params());

		if (didContinuationUri != null) {

			originalTransaction.addOutput(Coin.ZERO, new URIScriptBuilder(didContinuationUri).build());
		}

		SendRequest sendRequest = SendRequest.forTx(originalTransaction);

		try {

			walletAppKit.wallet().completeTx(sendRequest);
		} catch (InsufficientMoneyException ex) {

			Address currentReceiveAddress = walletAppKit.wallet().currentReceiveAddress();
			throw new RegistrationException("Insufficent coins: " + ex.getMessage() + " (Send coins to " + currentReceiveAddress + ")");
		}

		if (log.isDebugEnabled()) log.debug("Send request: " + sendRequest.toString());
		if (log.isDebugEnabled()) log.debug("Send request transaction: " + sendRequest.tx);

		// send transaction

		TransactionBroadcast transactionBroadcast = walletAppKit.peerGroup().broadcastTransaction(originalTransaction);
		if (log.isDebugEnabled()) log.debug("Transaction broadcast: " + transactionBroadcast);

		Transaction transaction;
		String transactionHash;

		transaction = sendRequest.tx;
		transactionHash = transaction.getTxId().toString();

		/*		try {

			ListenableFuture<Transaction> future = transactionBroadcast.future();
			transaction = future.get();
			transactionHash = transaction.getTxId().toString();
		} catch (InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot sent transaction: " + ex.getMessage());
		}*/

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

		DidBtcrJob job = new DidBtcrJob(chain, transactionHash, didContinuationUri, privateKeyAsWif, privateKeyAsHex, registerRequest.getAddServices(), registerRequest.getAddPublicKeys(), registerRequest.getAddAuthentications());
		jobId = this.newJob(job);

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("chain", chain);
		methodMetadata.put("transactionHash", transactionHash);
		methodMetadata.put("balance", Double.valueOf(balance));
		methodMetadata.put("changeAddress", "" + changeAddress);
		methodMetadata.put("didContinuationUri", "" + didContinuationUri);

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
		List<Service> addServices = job.getAddServices();
		List<PublicKey> addPublicKeys = job.getAddPublicKeys();
		List<Authentication> addAuthentications = job.getAddAuthentications();

		// read options

		// determine txref and DID

		ChainAndLocationData chainAndLocationData;
		String txref;
		String did;

		try {

			chainAndLocationData = this.getBitcoinConnection().lookupChainAndLocationData(Chain.valueOf(chain), transactionHash, 0);
			txref = chainAndLocationData == null ? null : ChainAndLocationData.txrefEncode(chainAndLocationData);
			did = txref == null ? null : "did:btcr:" + stripTxref(txref);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot determine txref: " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Determined chainAndBlockLocation: " + chainAndLocationData + ", txref: " + txref + ", DID: " + did);

		// REGISTER STATE: wait

		if (txref == null) {

			Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
			methodMetadata.put("chain", chain);
			methodMetadata.put("transactionHash", transactionHash);

			RegisterState registerState = new RegisterStateWaitDidBtcrConfirm(jobId, null, methodMetadata);
			return registerState;
		}

		// store continuation DID Document

		if (didContinuationUri != null) {

			if (log.isDebugEnabled()) log.debug("Storing continuation DID Document: " + didContinuationUri);

			DIDDocument didContinuationDocument = DIDDocument.build(did, addPublicKeys, addAuthentications, addServices);

			try {

				this.didDocContinuation.storeDIDDocContinuation(didContinuationUri, didContinuationDocument);
			} catch (IOException ex) {

				throw new RegistrationException("Cannot store continuation DID Document: " + ex.getMessage(), ex);
			}
		}

		// REGISTRATION STATE: finished

		this.finishJob(jobId);

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("chain", chain);
		methodMetadata.put("transactionHash", transactionHash);
		methodMetadata.put("blockHeight", chainAndLocationData.getLocationData().getBlockHeight());
		methodMetadata.put("transactionPosition", chainAndLocationData.getLocationData().getTransactionPosition());
		methodMetadata.put("txoIndex", chainAndLocationData.getLocationData().getTxoIndex());
		methodMetadata.put("didContinuationUri", "" + didContinuationUri);

		String identifier = did;

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
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		Map<String, Object> properties = new HashMap<String, Object> (this.getProperties());
		if (properties.containsKey("privateKeyMainnet")) properties.put("privateKeyMainnet", ((String) properties.get("privateKeyMainnet")).replaceAll(".", "."));
		if (properties.containsKey("privateKeyTestnet")) properties.put("privateKeyTestnet", ((String) properties.get("privateKeyTestnet")).replaceAll(".", "."));

		return properties;
	}

	private void openWalletAppKits() throws RegistrationException {

		// open wallet app kits

		NetworkParameters mainnetParams = MainNetParams.get();
		NetworkParameters testnetParams = TestNet3Params.get();
		URI uriMainnet = (this.getPeerMainnet() != null && this.getPeerMainnet().isEmpty()) ? URI.create("temp://" + this.getPeerMainnet()) : null;
		URI uriTestnet = (this.getPeerTestnet() != null && this.getPeerTestnet().isEmpty()) ? URI.create("temp://" + this.getPeerTestnet()) : null;

		this.walletAppKitMainnet = new WalletAppKit(mainnetParams, new File("./wallet/"), "mainNetWallet") {

			@Override
			protected void onSetupCompleted() {

				if (DidBtcrDriver.this.getPrivateKeyMainnet() != null && ! DidBtcrDriver.this.getPrivateKeyMainnet().isEmpty()) {

					ECKey privateKey = DumpedPrivateKey.fromBase58(this.params(), DidBtcrDriver.this.getPrivateKeyMainnet()).getKey();
					boolean success = wallet().importKey(privateKey);
					if (log.isInfoEnabled()) log.info("Imported private key for mainnet (" + success + "): " + wallet().getImportedKeys() + " - " + Address.fromKey(this.params(), privateKey, ScriptType.P2PKH));
				}
			}
		};
		if (uriMainnet != null) this.walletAppKitMainnet.setPeerNodes((new PeerAddress(mainnetParams, uriMainnet.getHost(), uriMainnet.getPort())));
		if (log.isInfoEnabled()) log.info("Opened mainnet wallet app kit: " + this.getPeerMainnet());

		this.walletAppKitTestnet = new WalletAppKit(testnetParams, new File("./wallet/"), "testNetWallet") {

			@Override
			protected void onSetupCompleted() {

				if (DidBtcrDriver.this.getPrivateKeyTestnet() != null && ! DidBtcrDriver.this.getPrivateKeyTestnet().isEmpty()) {

					ECKey privateKey = DumpedPrivateKey.fromBase58(this.params(), DidBtcrDriver.this.getPrivateKeyTestnet()).getKey();
					boolean success = wallet().importKey(privateKey);
					if (log.isInfoEnabled()) log.info("Imported private key for testnet (" + success + "): " + wallet().getImportedKeys() + " - " + Address.fromKey(this.params(), privateKey, ScriptType.P2PKH));
				}
			}
		};
		if (uriTestnet != null) this.walletAppKitTestnet.setPeerNodes((new PeerAddress(testnetParams, uriTestnet.getHost(), uriTestnet.getPort())));
		if (log.isInfoEnabled()) log.info("Opened testnet wallet app kit: " + this.getPeerTestnet());

		// connect

		this.walletAppKitMainnet.startAsync();
		this.walletAppKitTestnet.startAsync();
		this.walletAppKitMainnet.awaitRunning();
		this.walletAppKitTestnet.awaitRunning();

		// import keys

		if (log.isInfoEnabled()) log.info("Connected mainnet wallet app kit: " + this.getPeerMainnet() + " with balance " + this.walletAppKitMainnet.wallet().getBalance());
		if (log.isInfoEnabled()) log.info("Connected testnet wallet app kit: " + this.getPeerTestnet() + " with balance " + this.walletAppKitTestnet.wallet().getBalance());
	}

	/*
	 * Helper methods
	 */

	private static String stripTxref(String txref) {

		return txref.substring(txref.indexOf(":") + 1);
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
		private List<Service> addServices;
		private List<PublicKey> addPublicKeys;
		private List<Authentication> addAuthentications;

		private DidBtcrJob(String chain, String transactionHash, URI didContinuationUri, String privateKeyAsWif, String privateKeyAsHex, List<Service> addServices, List<PublicKey> addPublicKeys, List<Authentication> addAuthentications) {

			this.chain = chain;
			this.transactionHash = transactionHash;
			this.didContinuationUri = didContinuationUri;
			this.privateKeyAsWif = privateKeyAsWif;
			this.privateKeyAsHex = privateKeyAsHex;
			this.addServices = addServices;
			this.addPublicKeys = addPublicKeys;
			this.addAuthentications = addAuthentications;
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

		private List<Service> getAddServices() {

			return this.addServices;
		}

		private List<PublicKey> getAddPublicKeys() {

			return this.addPublicKeys;
		}

		private List<Authentication> getAddAuthentications() {

			return this.addAuthentications;
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

	public String getPrivateKeyMainnet() {

		return this.privateKeyMainnet;
	}

	public void setPrivateKeyMainnet(String privateKeyMainnet) {

		this.privateKeyMainnet = privateKeyMainnet;
	}

	public String getPrivateKeyTestnet() {

		return this.privateKeyTestnet;
	}

	public void setPrivateKeyTestnet(String privateKeyTestnet) {

		this.privateKeyTestnet = privateKeyTestnet;
	}

	public DIDDocContinuation getDidDocContinuation() {

		return this.didDocContinuation;
	}

	public void setDidDocContinuation(DIDDocContinuation didDocContinuation) {

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
