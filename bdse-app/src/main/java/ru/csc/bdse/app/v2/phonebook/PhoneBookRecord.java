package ru.csc.bdse.app.v2.phonebook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.csc.bdse.app.base.Record;

import java.util.*;

public class PhoneBookRecord extends ru.csc.bdse.app.v1.phonebook.PhoneBookRecord implements Record {
    private static class Extension {
        private String nickname;
        private List<String> phones = Collections.emptyList();

        private Extension() { }

        private Extension(String nickname, List<String> phones) {
            this.nickname = nickname;
            this.phones = phones;
        }

        protected String getNickname() {
            return nickname;
        }

        protected List<String> getPhones() {
            return phones;
        }
    }

    private final Extension extension;

    protected PhoneBookRecord() {
        super();
        extension = new Extension();
    }

    public PhoneBookRecord(String name, String surname, String nickname, List<String> phones) {
        super(name, surname, phones.get(0));
        this.extension = new Extension(nickname, phones.subList(1, phones.size()));
    }

    protected Extension getExtension() {
        return extension;
    }

    @JsonIgnore
    public String getNickname() {
        return extension.nickname;
    }

    @JsonIgnore
    public List<String> getPhones() {
        List<String> phones = new ArrayList<>();
        phones.add(getPhone());
        phones.addAll(extension.phones);
        return phones;
    }

    @Override
    public String toString() {
        return "PhoneBookRecord{" +
                "name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", nickname='" + getNickname() + '\'' +
                ", phones='" + getPhones() + '\'' +
                '}';
    }

    @Override
    public Set<Character> literals() {
        Set<Character> result = new HashSet<>(super.literals());
        result.add(getNickname().charAt(0));
        return result;
    }
}
