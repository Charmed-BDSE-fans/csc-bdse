package ru.csc.bdse.app.common;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.csc.bdse.util.Constants;

import java.util.Set;

public class PhoneBookApiHttpClientBase<R extends Record> implements PhoneBookApi<R> {
    private final String baseUrl;
    private final ParameterizedTypeReference<Set<R>> recordSetType;
    private final RestTemplate rest = new RestTemplate();

    private static final ParameterizedTypeReference<byte[]> BYTE_ARRAY_TYPE
            = new ParameterizedTypeReference<byte[]>() {};

    public PhoneBookApiHttpClientBase(String baseUrl, ParameterizedTypeReference<Set<R>> recordSetType) {
        this.baseUrl = baseUrl;
        this.recordSetType = recordSetType;
    }

    @Override
    public void put(R record) {
        String url = baseUrl + "/phonebook/";
        ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.PUT, record, BYTE_ARRAY_TYPE);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public void delete(R record) {
        String url = baseUrl + "/phonebook";
        ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, record, BYTE_ARRAY_TYPE);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public Set<R> get(char literal) {
        String url = baseUrl + "/phonebook/" + literal;
        ResponseEntity<Set<R>> responseEntity = request(url, HttpMethod.GET, null, recordSetType);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
        return responseEntity.getBody();
    }

    @Override
    public void deleteAll() {
        final String url = baseUrl + "/deleteAll";
        final ResponseEntity<byte[]> responseEntity = request(
                url, HttpMethod.DELETE,
                Constants.EMPTY_BYTE_ARRAY,
                BYTE_ARRAY_TYPE
        );
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    private <T, U> ResponseEntity<U> request(String url, HttpMethod method, T body, ParameterizedTypeReference<U> typeReference) {
        try {
            return rest.exchange(url, method, new HttpEntity<>(body), typeReference);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>((U) null, e.getStatusCode());
        }
    }
}
