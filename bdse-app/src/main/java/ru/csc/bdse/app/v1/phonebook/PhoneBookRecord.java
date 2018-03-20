package ru.csc.bdse.app.v1.phonebook;

import ru.csc.bdse.app.common.Record;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PhoneBookRecord implements Record {
    private String name;
    private String surname;
    private String phone;

    protected PhoneBookRecord() { }

    public PhoneBookRecord(String name, String surname, String phone) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneBookRecord record = (PhoneBookRecord) o;
        return  Objects.equals(name, record.name) &&
                Objects.equals(surname, record.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(String.format("%s-%s", name, surname));
    }

    @Override
    public String toString() {
        return "PhoneBookRecord{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public Set<Character> literals() {
        return Collections.singleton(surname.charAt(0));
    }
}
