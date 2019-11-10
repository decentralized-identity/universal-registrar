package uniregistrar.driver.did.sov;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.NaCl.Sodium;
import org.apache.commons.lang3.RandomStringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidAlreadyExistsException;
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

import com.github.jsonldjava.utils.JsonUtils;

import did.Service;
import io.leonard.Base58;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateFinished;
import uniregistrar.state.UpdateState;

public class DidSovDriver extends AbstractDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidSovDriver.class);

	private Map<String, Object> properties;

	private String libIndyPath;
	private String poolConfigs;
	private String poolVersions;
	private String walletName;
	private String trustAnchorSeed;

	private Map<String, Pool> poolMap = null;
	private Map<String, Integer> poolVersionMap = null;
	private Map<String, String> poolTaaMap = null;
	private Wallet wallet = null;
	private String trustAnchorDid = null;

	static {
		
		NaCl.init();
	}
	
	public DidSovDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidSovDriver() {

		this(getPropertiesFromEnvironment());
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

		synchronized (this) {

			if (this.getPoolMap() == null || this.getWallet() == null || this.getTrustAnchorDid() == null) this.openIndy();
		}

		// read options

		String network = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("network");
		if (network == null || network.trim().isEmpty()) network = "_";

		// find pool and version and taa

		Pool pool = this.getPoolMap().get(network);
		if (pool == null) throw new RegistrationException("Unknown network: " + network);

		Integer poolVersion = this.getPoolVersionMap().get(network);

		String taa = this.getPoolTaaMap().get(network);

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
				String nymRequest = Ledger.buildNymRequest(this.getTrustAnchorDid(), newDid, newVerkey, /*"{\"alias\":\"b\"}"*/ null, null).get();
				if (log.isDebugEnabled()) log.debug("nymRequest: " + nymRequest);

				// agree

				if (taa != null) {

					nymRequest = Taa.agree(nymRequest, taa);
					if (log.isDebugEnabled()) log.debug("agreed nymRequest: " + nymRequest);
				}

				// sign and submit request to ledger

				if (log.isDebugEnabled()) log.debug("=== SUBMIT 1 ===");
				String submitRequestResult1 = Ledger.signAndSubmitRequest(pool, this.getWallet(), this.getTrustAnchorDid(), nymRequest).get();
				if (log.isDebugEnabled()) log.debug("SubmitRequestResult1: " + submitRequestResult1);

				// service endpoints

				if (registerRequest.getAddServices() != null) {

					Map<String, Object> jsonObject = new HashMap<String, Object> ();
					Map<String, Object> endpointJsonObject = new HashMap<String, Object> ();

					for (Service service : registerRequest.getAddServices()) {

						endpointJsonObject.put(service.getType(), service.getServiceEndpoint());
					}

					jsonObject.put("endpoint", endpointJsonObject);

					String jsonObjectString;

					try {

						jsonObjectString = JsonUtils.toString(jsonObject);
					} catch (IOException ex) {

						throw new RegistrationException("Invalid endpoints: " + endpointJsonObject);
					}

					if (log.isDebugEnabled()) log.debug("Raw: " + jsonObjectString);

					// create ATTRIB request

					if (log.isDebugEnabled()) log.debug("=== CREATE ATTRIB REQUEST ===");
					String attribRequest = Ledger.buildAttribRequest(newDid, newDid, null, jsonObjectString, null).get();
					if (log.isDebugEnabled()) log.debug("attribRequest: " + attribRequest);

					// agree

					if (taa != null) {

						attribRequest = Taa.agree(attribRequest, taa);
						if (log.isDebugEnabled()) log.debug("agreed attribRequest: " + attribRequest);
					}

					// sign and submit request to ledger

					if (log.isDebugEnabled()) log.debug("=== SUBMIT 2 ===");
					String submitRequestResult2 = Ledger.signAndSubmitRequest(pool, walletUser, newDid, attribRequest).get();
					if (log.isDebugEnabled()) log.debug("SubmitRequestResult2: " + submitRequestResult2);
				}
			}
		} catch (InterruptedException | ExecutionException | IndyException ex) {

			throw new RegistrationException("Problem connecting to Indy: " + ex.getMessage(), ex);
		}

		// secret

		byte[] naclPublicKey = new byte[Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];
		byte[] naclSecretKey = new byte[Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
		NaCl.sodium().crypto_sign_ed25519_seed_keypair(naclPublicKey, naclSecretKey, newSeed.getBytes());
		byte[] naclDid = Arrays.copyOf(naclPublicKey, 16);

		// REGISTRATION STATE: finished

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("network", network);
		methodMetadata.put("poolVersion", poolVersion);
		methodMetadata.put("submitterDid", this.getTrustAnchorDid());

		String identifier = "did:sov:";
		if (network != null && ! network.isEmpty() && ! network.equals("_")) identifier += network + ":";
		identifier += newDid;

		Map<String, Object> secret = new LinkedHashMap<String, Object> ();
		secret.put("seed", newSeed);
		secret.put("naclPublicKey", Base58.encode(naclPublicKey));
		secret.put("naclSecretKey", Base58.encode(naclSecretKey));
		secret.put("naclDid", Base58.encode(naclDid));

		RegisterState registerState = RegisterState.build();
		SetRegisterStateFinished.setStateFinished(registerState, identifier, secret);
		registerState.setMethodMetadata(methodMetadata);
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
		if (properties.containsKey("trustAnchorSeed")) properties.put("trustAnchorSeed", ((String) properties.get("trustAnchorSeed")).replaceAll(".", "."));

		return properties;
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

		if (poolConfigMap.size() < 1) {

			throw new RegistrationException("Please provide pool configs for the did:sov: driver.");
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

		if (this.poolVersionMap.size() < 1) {

			throw new RegistrationException("Please provide pool versions for the did:sov: driver.");
		}

		if (log.isInfoEnabled()) log.info("Pool version map: " + this.poolVersionMap);

		// check trust anchor seed

		String trustAnchorSeed = this.getTrustAnchorSeed();

		if (trustAnchorSeed == null || trustAnchorSeed.trim().isEmpty()) {

			throw new RegistrationException("Please provide a trust anchor seed for the did:sov: driver.");
		}

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

		// open wallet

		try {

			String walletConfig = "{ \"id\":\"" + this.getWalletName() + "\", \"storage_type\":\"" + "default" + "\"}";
			String walletCredentials = "{ \"key\":\"key\" }";
			this.wallet = Wallet.openWallet(walletConfig, walletCredentials).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot open wallet \"" + this.getWalletName() + "\": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Opened wallet: " + this.wallet);

		// create trust anchor DID

		try {

			CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, trustAnchorSeed, null, null);
			CreateAndStoreMyDidResult createAndStoreMyDidResultTrustee = Did.createAndStoreMyDid(this.getWallet(), createAndStoreMyDidJSONParameterTrustee.toJson()).get();
			this.trustAnchorDid = createAndStoreMyDidResultTrustee.getDid();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof DidAlreadyExistsException) {

				if (log.isInfoEnabled()) log.info("Trust anchor DID has already been created.");
			} else {

				throw new RegistrationException("Cannot create trust anchor DID: " + ex.getMessage(), ex);
			}
		}

		if (log.isInfoEnabled()) log.info("Trust anchor DID: " + this.trustAnchorDid);

		// open pools

		this.poolMap = new HashMap<String, Pool> ();
		this.poolTaaMap = new HashMap<String, String> ();

		for (String poolConfigName : poolConfigMap.keySet()) {

			try {

				Pool.setProtocolVersion(this.getPoolVersionMap().get(poolConfigName));

				OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(null, null);
				Pool pool = Pool.openPoolLedger(poolConfigName, openPoolLedgerJSONParameter.toJson()).get();

				String taa = Taa.getTaa(pool, this.getWallet(), this.getTrustAnchorDid());

				this.poolMap.put(poolConfigName, pool);
				if (taa != null) this.poolTaaMap.put(poolConfigName, taa);
			} catch (IndyException | InterruptedException | ExecutionException ex) {

				if (log.isWarnEnabled()) log.warn("Cannot open pool \"" + poolConfigName + "\": " + ex.getMessage(), ex);
				continue;
			}
		}

		if (log.isInfoEnabled()) log.info("Opened " + this.poolMap.size() + " pools: " + this.poolMap.keySet());
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

	public Map<String, String> getPoolTaaMap() {

		return this.poolTaaMap;
	}

	public void setPoolTaaMap(Map<String, String> poolTaaMap) {

		this.poolTaaMap = poolTaaMap;
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
