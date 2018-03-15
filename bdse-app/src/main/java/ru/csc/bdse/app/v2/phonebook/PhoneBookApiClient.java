package ru.csc.bdse.app.v2.phonebook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import ru.csc.bdse.app.common.PhoneBookApiClientBase;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PhoneBookApiClient extends PhoneBookApiClientBase<PhoneBookRecord> {
    private static final String NICKNAME_INFIX = "nickname/";

    public PhoneBookApiClient(KeyValueApi kva) {
        super(kva);
    }

    private String makeLetterNicknameKey(Character literal) {
        return LETTER_PREFIX + NICKNAME_INFIX + literal;
    }

    private Set<PhoneBookRecord> getLetter(String identifier) {
        return keyValueApi.getKeys(identifier).stream().map((key) -> {
            try {
                // no check before get() since we know it's from
                // a valid collection
                return mapper.readValue(keyValueApi.get(key).get(), PhoneBookRecord.class);
            } catch (JsonParseException e) {
                LOGGER.warn("Bad json syntax in the record with key ", key);
                return null;
            } catch (JsonMappingException e) {
                LOGGER.warn("Bad json formatting in the record with key ", key);
                return null;
            } catch (IOException e) {
                LOGGER.warn("Failed to decode the record with key ", key);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void put(PhoneBookRecord record) {
        String keyData = makeDataKey(record.getId());

        String surname = record.getSurname();
        if(!Objects.equals(surname, "")) {
            String key = makeLetterKey(surname.charAt(0));
            keyValueApi.put(key, keyData.getBytes());
        }

        String nickname = record.getNickname();
        if(!Objects.equals(NICKNAME_INFIX + nickname, "")) {
            String key = makeLetterNicknameKey(nickname.charAt(0));
            keyValueApi.put(key, keyData.getBytes());
        }

        byte[] value;
        try {
            value = mapper.writeValueAsBytes(record);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to put due to a bad JSON");
            return;
        }

        keyValueApi.put(keyData, value);
    }

    @Override
    public void delete(PhoneBookRecord record) {
        String keyData = makeDataKey(record.getId());

        String surname = record.getSurname();
        if(!Objects.equals(surname, "")) {
            String key = makeLetterKey(surname.charAt(0));
            keyValueApi.delete(key);
        }

        String nickname = record.getNickname();
        if(!Objects.equals(nickname, "")) {
            String key = makeLetterNicknameKey(nickname.charAt(0));
            keyValueApi.delete(key);
        }

        keyValueApi.delete(keyData);
    }

    @Override
    public Set<PhoneBookRecord> get(char literal) {
        String keyLetterForSurname = makeLetterKey(literal);
        String keyLetterForNickname = makeLetterNicknameKey(literal);

        Set<PhoneBookRecord> bySurname = getLetter(keyLetterForSurname);
        Set<PhoneBookRecord> byNickname = getLetter(keyLetterForNickname);

        bySurname.addAll(byNickname);
        return bySurname;
    }
}
