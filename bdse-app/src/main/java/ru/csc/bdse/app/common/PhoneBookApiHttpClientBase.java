package ru.csc.bdse.app.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.csc.bdse.app.utils.SerializationUtils;
import ru.csc.bdse.util.Constants;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PhoneBookApiHttpClientBase<R extends Record> implements PhoneBookApi<R> {
    private final ObjectMapper objectMapper = SerializationUtils.getObjectMapperForRecords();
    private final String baseUrl;
    private final Class<R[]> recordArrayClass;
    private final RestTemplate rest = new RestTemplate();

    public PhoneBookApiHttpClientBase(String baseUrl, Class<R> recordClass) {
        @SuppressWarnings("unchecked")
        Class<R[]> aClass = (Class<R[]>) Array.newInstance(recordClass, 0).getClass();

        this.baseUrl = baseUrl;
        this.recordArrayClass = aClass;
    }

    @Override
    public void put(R record) {
        String url = baseUrl + "/phonebook/";
        ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.PUT, record);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public void delete(R record) {
        String url = baseUrl + "/phonebook";
        ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, record);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public Set<R> get(char literal) {
        String url = baseUrl + "/phonebook/" + literal;
        ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, null);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }

        return new HashSet<>(Arrays.asList(readAs(responseEntity.getBody(), recordArrayClass)));
    }

    @Override
    public void deleteAll() {
        final String url = baseUrl + "/deleteAll";
        final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, Constants.EMPTY_BYTE_ARRAY);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    private <T> ResponseEntity<byte[]> request(String url, HttpMethod method, T body) {
        try {
            return rest.exchange(url, method, new HttpEntity<>(body), byte[].class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(new byte[0], e.getStatusCode());
        }
    }

    private <T> T readAs(byte[] src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Response error: " + e.getMessage());
        }
    }
}
