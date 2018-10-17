package uniregistrar.driver.did.sov;

import java.io.File;
import java.security.acl.Owner;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.request.RegistrationRequest;
import uniregistrar.state.RegistrationState;

public class DidSovDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidSovDriver.class);

	private Map<String, Object> properties;

	private static final Gson gson = new Gson();

	private String libIndyPath;
	private String poolConfigs;
	private String poolVersions;
	private String walletName;

	private Map<String, Pool> poolMap = null;
	private Map<String, Integer> poolVersionMap = null;
	private Wallet wallet = null;
	private String submitterDid = null;

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

			String env_libIndyPath = System.getenv("uniresolver_driver_did_sov_libIndyPath");
			String env_poolConfigs = System.getenv("uniresolver_driver_did_sov_poolConfigs");
			String env_poolVersions = System.getenv("uniresolver_driver_did_sov_poolVersions");
			String env_walletName = System.getenv("uniresolver_driver_did_sov_walletName");

			if (env_libIndyPath != null) properties.put("libIndyPath", env_libIndyPath);
			if (env_poolConfigs != null) properties.put("poolConfigs", env_poolConfigs);
			if (env_poolVersions != null) properties.put("poolVersions", env_poolVersions);
			if (env_walletName != null) properties.put("walletName", env_walletName);
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

			if (prop_libIndyPath != null) this.setLibIndyPath(prop_libIndyPath);
			if (prop_poolConfigs != null) this.setPoolConfigs(prop_poolConfigs);
			if (prop_poolVersions != null) this.setPoolVersions(prop_poolVersions);
			if (prop_walletName != null) this.setWalletName(prop_walletName);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegistrationState register(RegistrationRequest registrationRequest) throws RegistrationException {

		// open pool

		if (this.getPoolMap() == null || this.getWallet() == null || this.getSubmitterDid() == null) this.openIndy();

		// parse identifier

		Matcher matcher = DID_SOV_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String targetDid = matcher.group(1);

		// send GET_NYM request

		String getNymResponse;

		try {

			String getNymRequest = Ledger.buildGetNymRequest(this.getSubmitterDid(), targetDid).get();
			getNymResponse = Ledger.signAndSubmitRequest(this.getPool(), this.getWallet(), this.getSubmitterDid(), getNymRequest).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot send GET_NYM request: " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("GET_NYM for " + targetDid + ": " + getNymResponse);

		// send GET_ATTR request

		String getAttrResponse;

		try {

			String getAttrRequest = Ledger.buildGetAttribRequest(this.getSubmitterDid(), targetDid, "endpoint").get();
			getAttrResponse = Ledger.signAndSubmitRequest(this.getPool(), this.getWallet(), this.getSubmitterDid(), getAttrRequest).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot send GET_NYM request: " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("GET_ATTR for " + targetDid + ": " + getAttrResponse);

		// DDO id

		String id = identifier;

		// DDO owners

		JsonObject jsonGetNymResponse = gson.fromJson(getNymResponse, JsonObject.class);
		JsonObject jsonGetNymResult = jsonGetNymResponse == null ? null : jsonGetNymResponse.getAsJsonObject("result");
		JsonElement jsonGetNymData = jsonGetNymResult == null ? null : jsonGetNymResult.get("data");
		JsonObject jsonGetNymDataContent = (jsonGetNymData == null || jsonGetNymData instanceof JsonNull) ? null : gson.fromJson(jsonGetNymData.getAsString(), JsonObject.class);
		JsonPrimitive jsonGetNymVerkey = jsonGetNymDataContent == null ? null : jsonGetNymDataContent.getAsJsonPrimitive("verkey");

		String verkey = jsonGetNymVerkey == null ? null : jsonGetNymVerkey.getAsString();

		Owner owner = Owner.build(identifier, DDO_OWNER_TYPES, DDO_CURVE, verkey, null);

		List<RegistrationState.Owner> owners = Collections.singletonList(owner);

		// DDO controls

		List<RegistrationState.Control> controls = Collections.emptyList();

		// DDO services

		JsonObject jsonGetAttrResponse = gson.fromJson(getAttrResponse, JsonObject.class);
		JsonObject jsonGetAttrResult = jsonGetAttrResponse == null ? null : jsonGetAttrResponse.getAsJsonObject("result");
		JsonElement jsonGetAttrData = jsonGetAttrResult == null ? null : jsonGetAttrResult.get("data");
		JsonObject jsonGetAttrDataContent = (jsonGetAttrData == null || jsonGetAttrData instanceof JsonNull) ? null : gson.fromJson(jsonGetAttrData.getAsString(), JsonObject.class);
		JsonObject jsonGetAttrEndpoint = jsonGetAttrDataContent == null ? null : jsonGetAttrDataContent.getAsJsonObject("endpoint");

		Map<String, String> services = new HashMap<String, String> ();

		for (Map.Entry<String, JsonElement> jsonService : jsonGetAttrEndpoint.entrySet()) {

			JsonPrimitive jsonGetAttrEndpointValue = jsonGetAttrEndpoint == null ? null : jsonGetAttrEndpoint.getAsJsonPrimitive(jsonService.getKey());
			String value = jsonGetAttrEndpointValue == null ? null : jsonGetAttrEndpointValue.getAsString();

			services.put(jsonService.getKey(), value);
		}

		// create DDO

		RegistrationState ddo = RegistrationState.build(id, owners, controls, services);

		// done

		return ddo;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	private void openIndy() throws ResolutionException {

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

		// create submitter DID

		try {

			CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, null, null, null);
			CreateAndStoreMyDidResult createAndStoreMyDidResultTrustee = Did.createAndStoreMyDid(this.getWallet(), createAndStoreMyDidJSONParameterTrustee.toJson()).get();
			this.submitterDid = createAndStoreMyDidResultTrustee.getDid();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new RegistrationException("Cannot create submitter DID: " + ex.getMessage(), ex);
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

	public String getSubmitterDid() {

		return this.submitterDid;
	}

	public void setSubmitterDid(String submitterDid) {

		this.submitterDid = submitterDid;
	}
}
