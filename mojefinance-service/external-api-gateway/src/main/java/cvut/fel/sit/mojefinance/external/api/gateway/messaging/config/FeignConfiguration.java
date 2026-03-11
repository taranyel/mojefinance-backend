package cvut.fel.sit.mojefinance.external.api.gateway.messaging.config;

import feign.Client;
import feign.hc5.ApacheHttp5Client;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
@RequiredArgsConstructor
public class FeignConfiguration {
    private final SslBundles sslBundles;

    public Client createFeignClient(String bundleName) {
        var sslBundle = sslBundles.getBundle(bundleName);
        SSLContext sslContext = sslBundle.createSslContext();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build())
                .build();

        return new ApacheHttp5Client(httpClient);
    }
}
