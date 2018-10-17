package uniregistrar.driver.did.btcr;

import java.io.IOException;
import java.security.acl.Owner;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.weboftrust.txrefconversion.Bech32;
import info.weboftrust.txrefconversion.Chain;
import info.weboftrust.txrefconversion.ChainAndTxid;
import info.weboftrust.txrefconversion.TxrefConverter;
import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.btcr.bitcoinconnection.BTCDRPCBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoinConnection.BtcrData;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoindRPCBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import uniregistrar.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniregistrar.request.RegistrationRequest;
import uniregistrar.state.RegistrationState;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	private Map<String, Object> properties;

	private BitcoinConnection bitcoinConnection;

	private HttpClient httpClient = HttpClients.createDefault();

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

			String env_bitcoinConnection = System.getenv("uniresolver_driver_did_btcr_bitcoinConnection");
			String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlMainnet");
			String env_rpcUrlTestnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlTestnet");

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
			} else  if ("blockcypherapi".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BlockcypherAPIBitcoinConnection());
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegistrationState register(RegistrationRequest registrationRequest) throws RegistrationException {

		// parse identifier

		Matcher matcher = DID_BTCR_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String targetDid = matcher.group(1);

		// determine txref

		String txref = null;
		if (targetDid.charAt(0) == TxrefConverter.MAGIC_BTC_MAINNET_BECH32_CHAR) txref = TxrefConverter.TXREF_BECH32_HRP_MAINNET + Bech32.SEPARATOR + "-" + targetDid;
		if (targetDid.charAt(0) == TxrefConverter.MAGIC_BTC_TESTNET_BECH32_CHAR) txref = TxrefConverter.TXREF_BECH32_HRP_TESTNET + Bech32.SEPARATOR + "-" + targetDid;
		if (txref == null) throw new RegistrationException("Invalid magic byte in " + targetDid);

		// retrieve btcr data

		Chain chain;
		String txid;
		BtcrData btcrData;

		try {

			TxrefConverter txrefConverter = new TxrefConverter(this.getExtendedBitcoinConnection());

			ChainAndTxid chainAndTxid = txrefConverter.txrefToTxid(txref);
			chain = chainAndTxid.getChain();
			txid = chainAndTxid.getTxid();

			btcrData = this.getExtendedBitcoinConnection().getBtcrData(chain, txid);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve BTCR data for " + txref + ": " + ex.getMessage());
		}

		if (log.isInfoEnabled()) log.info("Retrieved BTCR data for " + txref + " ("+ txid + " on chain " + chain + "): " + btcrData);

		// retrieve more DDO

		HttpGet httpGet = new HttpGet(btcrData.getMoreDdoUri());
		RegistrationState moreDdo = null;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new RegistrationException("Cannot retrieve more DDO for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			moreDdo = RegistrationState.fromString(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve more DDO for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Retrieved more DDO for " + txref + " (" + btcrData.getMoreDdoUri() + "): " + moreDdo);

		// DDO id

		String id = identifier;

		// DDO owners

		Owner owner = Owner.build(identifier, DDO_OWNER_TYPES, DDO_CURVE, null, btcrData.getInputScriptPubKey());

		List<RegistrationState.Owner> owners = Collections.singletonList(owner);

		// DDO controls

		List<RegistrationState.Control> controls = Collections.emptyList();

		// DDO services

		Map<String, String> services = moreDdo.getServices();

		// create DDO

		RegistrationState ddo = RegistrationState.build(id, owners, controls, services);

		// done

		return ddo;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
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

	public BitcoinConnection getBitcoinConnection() {

		return this.bitcoinConnection;
	}

	public void setBitcoinConnection(BitcoinConnection bitcoinConnection) {

		this.bitcoinConnection = bitcoinConnection;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
