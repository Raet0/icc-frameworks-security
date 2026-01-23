package ec.edu.ups.icc.fundamentos01.security.models;

public class JwtProperties {

    private String secret;
    private Long expiration;
    private Long refreshExpiration;
    private String issuer;
    private String header;
    private String prefix;
    public String getSecret() {
        return secret;
    }
    public Long getExpiration() {
        return expiration;
    }
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
    public String getIssuer() {
        return issuer;
    }
    public String getHeader() {
        return header;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public void setHeader(String header) {
        this.header = header;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    // Getters y Setters

    
}
