package com.codit.cryptoconverter.util;

import java.util.LinkedHashMap;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class CryptoCurrency {

    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String BCH = "BCH";
    public static final String DASH = "DASH";
    public static final String LTC = "LTC";
    public static final String DOGE = "DOGE";
    public static final String XRP = "XRP";
    public static final String PRICE_NOT_AVAILABLE = "Not available";
    private static final double SATOSHI = Math.pow(10, 8);
    private static final double WEI = Math.pow(10, 18);
    private static LinkedHashMap<String, String> coinsData = new LinkedHashMap<>();

    static {
        coinsData.put("BTC", "Bitcoin");
        coinsData.put("ETH", "Ethereum");
        coinsData.put("XRP", "Ripple");
        coinsData.put("BCH", "Bitcoin Cash");
        coinsData.put("LTC", "Litecoin");
        coinsData.put("ADA", "Cardano");
        coinsData.put("XLM", "Stellar");
        coinsData.put("NEO", "NEO");
        coinsData.put("EOS", "EOS");
        coinsData.put("IOTA", "IOTA");
        coinsData.put("DASH", "DASH");
        coinsData.put("XEM", "NEM");
        coinsData.put("XMR", "Monero");
        coinsData.put("LSK", "LISK");
        coinsData.put("ETC", "Ethereum Classic");
        coinsData.put("VEN", "Ve Chain");
        coinsData.put("TRX", "TRON");
        coinsData.put("BTG", "Bitcoin Gold");
        coinsData.put("USDT", "Tether");
        coinsData.put("OMG", "OmiseGO");
        coinsData.put("ICX", "ICON");
        coinsData.put("ZEC", "Zcash");
        coinsData.put("XVG", "Verge");
        coinsData.put("BCN", "Bytecoin");
        coinsData.put("PPT", "Populous");
        coinsData.put("STRAT", "Stratis");
        coinsData.put("RHOC", "RChain");
        coinsData.put("SC", "Siacoin");
        coinsData.put("WAVES", "Waves");
        coinsData.put("SNT", "Status");
        coinsData.put("MKR", "Maker");
        coinsData.put("BTS", "BitShares");
        coinsData.put("VERI", "Veritaseum");
        coinsData.put("AE", "Aeternity");
        coinsData.put("WTC", "Waltonchain");
        coinsData.put("ZCL", "ZClassic");
        coinsData.put("DCR", "Decred");
        coinsData.put("REP", "Augur");
        coinsData.put("DGD", "DigixDAO");
        coinsData.put("ARDR", "Ardor");
        coinsData.put("HSR", "Hshare");
        coinsData.put("ETN", "Electroneum");
        coinsData.put("KMD", "Komodo");
        coinsData.put("GAS", "Gas");
        coinsData.put("ARK", "Ark");
        coinsData.put("BTX", "Bitcore");
        coinsData.put("VTC", "Vertcoin");
        coinsData.put("DOGE", "DogeCoin");
        coinsData.put("GNO", "GNO");
        coinsData.put("BCD", "BCD");
        coinsData.put("BAT", "BAT");
        coinsData.put("ARN", "ARN");
        coinsData.put("QTUM", "QTUM");
        coinsData.put("BIX", "BiboxCoin");
        coinsData.put("BNB", "BinanceCoin");
        coinsData.put("XUC", "Exchange Union");
        coinsData.put("HT", "Huobi Token");
        coinsData.put("QKC", "QuarkChain");
        coinsData.put("ZIL", "Zilliqa");
        coinsData.put("NANO", "NANO");
        coinsData.put("MITH", "Mithril");
        coinsData.put("IOST", "IOS Token");
        coinsData.put("QASH", "Quoine Liquid");
        coinsData.put("NAS", "Nebulas");
        coinsData.put("POWR", "Power Ledger");
        coinsData.put("IOTX", "IoTeX Network");
        coinsData.put("CVC", "Civic");
        coinsData.put("RDD", "Reddcoin");
        coinsData.put("OCN", "Odyssey");
        coinsData.put("ELF", "aelf");
        coinsData.put("CTXC", "Cortex");
        coinsData.put("STEEM", "Steem");
        coinsData.put("KNC", "Kyber Network");
        coinsData.put("SALT", "Salt Lending");
        coinsData.put("GTO", "GIFTO");
        coinsData.put("BLZ", "Bluezelle");
        coinsData.put("UBTC", "UnitedBitcoin");
        coinsData.put("XZC", "ZCoin");
        coinsData.put("GVT", "Genesis Vision");
        coinsData.put("NULS", "Nuls");
        coinsData.put("BOX", "ContentBox");
        coinsData.put("XIN", "Infinity Economics");
        coinsData.put("APIS", "APIS");
        coinsData.put("BMX", "Bitmart Coin");
        coinsData.put("PST", "Primas");
        coinsData.put("SWFTC", "SwftCoin");
        coinsData.put("WPR", "WePower");
        coinsData.put("VIBE", "VOIBEHub");
        coinsData.put("LYM", "Lympo");
        coinsData.put("EVX", "Everex");
        coinsData.put("VET", "Vechain");
        coinsData.put("VIB", "Viberate");
        coinsData.put("MDA", "Moeda");
        coinsData.put("ONT", "Ontology");
        coinsData.put("OKB", "Okex");
        coinsData.put("ETF", "EthereumFog");











    }

    public static LinkedHashMap<String, String> getCryptoCurrencyData() {
        return coinsData;
    }

    public static String getCoinName(String coinCode) {
        return coinsData.get(coinCode);
    }
}

