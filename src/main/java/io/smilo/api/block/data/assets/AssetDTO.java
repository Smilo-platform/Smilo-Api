package io.smilo.api.block.data.assets;

public class AssetDTO {
    private String address;
    private long totalSupply;
    private String name;
    private int decimals;
    private String symbol;

    public AssetDTO() {

    }
    public AssetDTO(String address, long totalSupply, String name, int decimals, String symbol) {
        this.address = address;
        this.totalSupply = totalSupply;
        this.name = name;
        this.decimals = decimals;
        this.symbol = symbol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public long getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
