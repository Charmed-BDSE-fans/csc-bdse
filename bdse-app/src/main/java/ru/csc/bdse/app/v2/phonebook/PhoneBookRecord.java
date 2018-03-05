package ru.csc.bdse.app.v2.phonebook;

import ru.csc.bdse.app.v1.phonebook.Record;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PhoneBookRecord implements Record {

    private final String name;
    private final String nickname;
    private final String surname;
    private final List<String> phone;

    public PhoneBookRecord(String name, String nickname, String surname, List<String> phone) {
        this.name = name;
        this.nickname = nickname;
        this.surname = surname;
        this.phone = phone;
    }

    @Override
    public Set<Character> literals() {
        LinkedHashSet<Character> s = new LinkedHashSet<>();
        s.add(surname.charAt(0));
        s.add(nickname.charAt(0));
        return  s;
    }
}
