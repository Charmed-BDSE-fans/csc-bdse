package ru.csc.bdse.app.v2.phonebook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.csc.bdse.app.common.Record;

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

        public String getNickname() {
            return nickname;
        }

        public List<String> getPhones() {
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

    public Extension getExtension() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PhoneBookRecord that = (PhoneBookRecord) o;
        return Objects.equals(getNickname(), that.getNickname());
    }

    @Override
    public int hashCode() {
        if (getNickname() == null) {
            return Objects.hash(String.format("%s-%s", getName(), getSurname()));
        }

        return Objects.hash(String.format("%s-%s-%s", getName(), getSurname(), getNickname()));
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
        if (getNickname() != null)
            result.add(getNickname().charAt(0));
        return result;
    }
}
