package uniregistrar.driver.did.sov;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.hyperledger.indy.sdk.IndyConstants;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.CreatePoolLedgerConfigJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolLedgerConfigExistsException;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateFinished;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public class DidSovDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidSovDriver.class);

	private Map<String, Object> properties;

	private String libIndyPath;
	private String poolConfigs;
	private String poolVersions;
	private String walletName;
	private String trustAnchorSeed;

	private Map<String, Pool> poolMap = null;
	private Map<String, Integer> poolVersionMap = null;
	private Wallet wallet = null;
	private String trustAnchorDid = null;

	public DidSovDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidSovDriver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_libIndyPath = System.getenv("uniregistrar_driver_did_sov_libIndyPath");
			String env_poolConfigs = System.getenv("uniregistrar_driver_did_sov_poolConfigs");
			String env_poolVersions = System.getenv("uniregistrar_driver_did_sov_poolVersions");
			String env_walletName = System.getenv("uniregistrar_driver_did_sov_walletName");
			String env_trustAnchorSeed = System.getenv("uniregistrar_driver_did_sov_trustAnchorSeed");

			if (env_libIndyPath != null) properties.put("libIndyPath", env_libIndyPath);
			if (env_poolConfigs != null) properties.put("poolConfigs", env_poolConfigs);
			if (env_poolVersions != null) properties.put("poolVersions", env_poolVersions);
			if (env_walletName != null) properties.put("walletName", env_walletName);
			if (env_trustAnchorSeed != null) properties.put("trustAnchorSeed", env_trustAnchorSeed);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_libIndyPath = (String) this.getProperties().get("libIndyPath");
			String prop_poolConfigs = (String) this.getProperties().get("poolConfigs");
			String prop_poolVersions = (String) this.getProperties().get("poolVersions");
			String prop_walletName = (String) this.getProperties().get("walletName");
			String prop_trustAnchorSeed = (String) this.getProperties().get("trustAnchorSeed");

			if (prop_libIndyPath != null) this.setLibIndyPath(prop_libIndyPath);
			if (prop_poolConfigs != null) this.setPoolConfigs(prop_poolConfigs);
			if (prop_poolVersions != null) this.setPoolVersions(prop_poolVersions);
			if (prop_walletName != null) this.setWalletName(prop_walletName);
			if (prop_trustAnchorSeed != null) this.setTrustAnchorSeed(prop_trustAnchorSeed);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// open pool and wallet

		if (this.getPoolMap() == null || this.getWallet() == null || this.getTrustAnchorDid() == null) this.openIndy();

		// read parameters

		String network = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("network");
		if (network == null || network.trim().isEmpty()) network = "_";

		// find pool and version

		Pool pool = this.getPoolMap().get(network);
		if (pool == null) throw new RegistrationException("Unknown network: " + network);

		Integer poolVersion = this.getPoolVersionMap().get(network);

		// create USER SEED

		String newSeed = RandomStringUtils.randomAlphanumeric(32);

		// register

		String newDid;
		String newVerkey;

		try {

			synchronized (this) {

				Pool.setProtocolVersion(poolVersion);

				// create USER DID

				Wallet walletUser = this.getWallet();

				if (log.isDebugEnabled()) log.debug("=== CREATE NYM REQUEST ===");
				CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, newSeed, null, null);
				if (log.isDebugEnabled()) log.debug("CreateAndStoreMyDidJSONParameter: " + createAndStoreMyDidJSONParameter);
				CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletUser, createAndStoreMyDidJSONParameter.toJson()).get();
				if (log.isDebugEnabled()) log.debug("CreateAndStoreMyDidResult: " + createAndStoreMyDidResult);

				newDid = createAndStoreMyDidResult.getDid();
				newVerkey = createAndStoreMyDidResult.getVerkey();

				// create NYM request

				if (log.isDebugEnabled()) log.debug("=== CREATE NYM REQUEST ===");
				String nymRequest = Ledger.buildNymRequest(this.getTrustAnchorDid(), newDid, newVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
				if (log.isDebugEnabled()) log.debug("nymRequest: " + nymRequest);

				// sign and submit request to ledger

				if (log.isDebugEnabled()) log.debug("=== SUBMIT 1 ===");
				String submitRequestResult1 = Ledger.signAndSubmitRequest(pool, this.getWallet(), this.getTrustAnchorDid(), nymRequest).get();
				if (log.isDebugEnabled()) log.debug("SubmitRequestResult1: " + submitRequestResult1);

				// create ATTRIB request

				if (log.isDebugEnabled()) log.debug("=== CREATE ATTRIB REQUEST ===");
				String attribRequest = Ledger.buildAttribRequest(newDid, newDid, null, "{\"endpoint\":{\"xdi\":\"http://127.0.0.1:8080/xdi\"}}", null).get();
				if (log.isDebugEnabled()) log.debug("attribRequest: " + attribRequest);

				// sign and submit request to ledger

				if (log.isDebugEnabled()) log.debug("=== SUBMIT 2 ===");
				String submitRequestResult2 = Ledger.signAndSubmitRequest(pool, walletUser, newDid, attribRequest).get();
				if (log.isDebugEnabled()) log.debug("SubmitRequestResult2: " + submitRequestResult2);
			}
		} catch (InterruptedException | ExecutionException | IndyException ex) {

			throw new RegistrationException("Problem connecting to Indy: " + ex.getMessage(), ex);
		}

		// create JOBID

		String jobId = null;

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("network", network);
		methodMetadata.put("poolVersion", poolVersion);
		methodMetadata.put("submitterDid", this.getTrustAnchorDid());

		// create IDENTIFIER

		String identifier = "did:sov:";
		if (network != null && ! network.isEmpty() && ! network.equals("_")) identifier += network + ":";
		identifier += newDid;

		// create CREDENTIALS

		Map<String, Object> credentials = new LinkedHashMap<String, Object> ();
		credentials.put("seed", newSeed);

		// create REGISTER STATE

		RegisterState registerState = new RegisterStateFinished(jobId, null, methodMetadata, identifier, credentials);

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

	private void openIndy() throws RegistrationException {

		// initialize libindy

		if ((! LibIndy.isInitialized()) && this.getLibIndyPath() != null) {

			if (log.isInfoEnabled()) log.info("Initializing libindy: " + this.getLibIndyPath() + " (" + new File(this.getLibIndyPath()).getAbsolutePath() + ")");
			LibIndy.init(this.getLibIndyPath());
		}

		// parse pool configs

		String[] poolConfigs = this.getPoolConfigs().split(";");
		Map<String, String> poolConfigMap = new HashMap<String, String> ();

		for (int i=0; i<poolConfigs.length; i+=2) {

			String poolConfigName = poolConfigs[i];
			String poolConfigFile = poolConfigs[i+1];

			poolConfigMap.put(poolConfigName, poolConfigFile);
		}

		if (log.isInfoEnabled()) log.info("Pool config map: " + poolConfigMap);

		// parse pool versions

		String[] poolVersions = this.getPoolVersions().split(";");
		this.poolVersionMap = new HashMap<String, Integer> ();

		for (int i=0; i<poolVersions.length; i+=2) {

			String poolConfigName = poolVersions[i];
			Integer poolConfigVersion = Integer.parseInt(poolVersions[i+1]);

			this.poolVersionMap.put(poolConfigName, poolConfigVersion);
		}

		if (log.isInfoEnabled()) log.info("Pool version map: " + this.poolVersionMap);

		// create pool configs

		for (Map.Entry<String, String> poolConfig : poolConfigMap.entrySet()) {

			String poolConfigName = poolConfig.getKey();
			String poolConfigFile = poolConfig.getValue();

			try {

				CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new CreatePoolLedgerConfigJSONParameter(poolConfigFile);
				Pool.createPoolLedgerConfig(poolConfigName, createPoolLedgerConfigJSONParameter.toJson()).get();
				if (log.isInfoEnabled()) log.info("Pool config \"" + poolConfigName + "\" successfully created.");
			} catch (IndyException | InterruptedException | ExecutionException ex) {

				IndyException iex = null;
				if (ex instanceof IndyException) iex = (IndyException) ex;
				if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
				if (iex instanceof PoolLedgerConfigExistsException) {

					if (log.isInfoEnabled()) log.info("Pool config \"" + poolConfigName + "\" has already been created.");
				} else {

					throw new RegistrationException("Cannot create pool config \"" + poolConfigName + "\": " + ex.getMessage(), ex);
				}
			}
		}

		// create wallet

		try {

			String walletConfig = "{ \"id\":\"" + this.getWalletName() + "\", \"storage_type\":\"" + "default" + "\"}";
			String walletCredentials = "{ \"key\":\"key\" }";
			Wallet.createWallet(walletConfig, walletCredentials).get();
			if (log.isInfoEnabled()) log.info("Wallet \"" + this.getWalletName() + "\" successfully created.");
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof WalletExistsException) {

				if (log.isInfoEnabled()) log.info("Wallet \"" + this.getWalletName() + "\" has already been created.");
			} else {

				throw new RegistrationException("Cannot create wallet \"" + this.getWalletName() + "\": " + ex.getMessage(), ex);
			}
		}

		// open pools

		this.poolMap = new HashMap<String, Pool> ();

		for (String poolConfigName : poolConfigMap.keySet()) {

			try {

				Pool.setProtocolVersion(this.getPoolVersionMap().get(poolConfigName));

				OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(null, null);
				Pool pool = Pool.openPoolLedger(poolConfigName, openPoolLedgerJSONParameter.toJson()).get();

				this.poolMap.put(poolConfigName, pool);
			} catch (IndyException | InterruptedException | ExecutionException ex) {

				throw new RegistrationException("Cannot open pool \"" + poolConfigName + "\": " + ex.getMessage(), ex);
			}
		}

		if (log.isInfoEnabled()) log.info("Opened " + this.poolMap.size() + " pools: " + this.poolMap.keySet());

		// open wallet

		try {

			String walletConfig = "{ \"id\":\"" + this.getWalletName() + "\", \"storage_type\":\"" + "default" + "\"}";
			String walletCredentials = "{ \"key\":\"key\" }";
			this.wallet = Wallet.openWallet(walletConfig, walletCredentials).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot open wallet \"" + this.getWalletName() + "\": " + ex.getMessage(), ex);
		}

		// create trust anchor DID

		try {

			CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, this.getTrustAnchorSeed(), null, null);
			CreateAndStoreMyDidResult createAndStoreMyDidResultTrustee = Did.createAndStoreMyDid(this.getWallet(), createAndStoreMyDidJSONParameterTrustee.toJson()).get();
			this.trustAnchorDid = createAndStoreMyDidResultTrustee.getDid();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot create trust anchor DID: " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Created trust anchor DID: " + this.trustAnchorDid);
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

	public String getLibIndyPath() {

		return this.libIndyPath;
	}

	public void setLibIndyPath(String libIndyPath) {

		this.libIndyPath = libIndyPath;
	}

	public String getPoolConfigs() {

		return this.poolConfigs;
	}

	public void setPoolConfigs(String poolConfigs) {

		this.poolConfigs = poolConfigs;
	}

	public String getPoolVersions() {

		return this.poolVersions;
	}

	public void setPoolVersions(String poolVersions) {

		this.poolVersions = poolVersions;
	}

	public String getWalletName() {

		return this.walletName;
	}

	public void setWalletName(String walletName) {

		this.walletName = walletName;
	}

	public String getTrustAnchorSeed() {

		return this.trustAnchorSeed;
	}

	public void setTrustAnchorSeed(String trustAnchorSeed) {

		this.trustAnchorSeed = trustAnchorSeed;
	}

	public Map<String, Pool> getPoolMap() {

		return this.poolMap;
	}

	public void setPoolMap(Map<String, Pool> poolMap) {

		this.poolMap = poolMap;
	}

	public Map<String, Integer> getPoolVersionMap() {

		return this.poolVersionMap;
	}

	public void setPoolVersionMap(Map<String, Integer> poolVersionMap) {

		this.poolVersionMap = poolVersionMap;
	}

	public Wallet getWallet() {

		return this.wallet;
	}

	public void setWallet(Wallet wallet) {

		this.wallet = wallet;
	}

	public String getTrustAnchorDid() {

		return this.trustAnchorDid;
	}

	public void setTrustAnchorDid(String trustAnchorDid) {

		this.trustAnchorDid = trustAnchorDid;
	}
}
