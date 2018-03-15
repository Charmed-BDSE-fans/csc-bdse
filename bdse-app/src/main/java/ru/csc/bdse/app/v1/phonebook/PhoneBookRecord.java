package ru.csc.bdse.app.v1.phonebook;

import ru.csc.bdse.app.common.Record;

import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class PhoneBookRecord implements Record {

    private int id;
    private String name;
    private String surname;
    private String phone;

    protected PhoneBookRecord() { }

    public PhoneBookRecord(String name, String surname, String phone) {
        Random rand = new Random();
        this.id = rand.nextInt();
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public int getId() {
        return id;
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
        return id == record.id &&
                Objects.equals(name, record.name) &&
                Objects.equals(surname, record.surname) &&
                Objects.equals(phone, record.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
