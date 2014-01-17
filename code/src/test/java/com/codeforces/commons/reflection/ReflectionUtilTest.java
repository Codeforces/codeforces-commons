package com.codeforces.commons.reflection;

import com.codeforces.commons.text.StringUtil;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 16.01.14
 */
@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "ProtectedField", "OverlyLongMethod"})
public class ReflectionUtilTest extends TestCase {
    public void testGetDeepValue() throws Exception {
        Person personMike = new Person(1L, 20, "Mike");
        Person personMax = new Person(2L, 21, "Max");
        User userIvan = new User(3L, 22, "Ivan", "TheBest", "12345");

        personMike.addFriend(personMax);
        personMike.addFriend(userIvan);

        personMax.addFriend(personMike);
        personMax.addFriend(userIvan);

        userIvan.addFriend(personMike);
        userIvan.addFriend(personMax);

        assertEquals("personMike.id mismatch.", 1L, ReflectionUtil.getDeepValue(personMike, "id"));
        assertEquals("personMike.age mismatch.", 20, ReflectionUtil.getDeepValue(personMike, "age"));
        assertEquals("personMike.name mismatch.", "Mike", ReflectionUtil.getDeepValue(personMike, "name"));

        assertEquals("personMax.id mismatch.", 2L, ReflectionUtil.getDeepValue(personMax, "id"));
        assertEquals("personMax.age mismatch.", 21, ReflectionUtil.getDeepValue(personMax, "age"));
        assertEquals("personMax.name mismatch.", "Max", ReflectionUtil.getDeepValue(personMax, "name"));

        assertEquals("userIvan.id mismatch.", 3L, ReflectionUtil.getDeepValue(userIvan, "id"));
        assertEquals("userIvan.age mismatch.", 22, ReflectionUtil.getDeepValue(userIvan, "age"));
        assertEquals("userIvan.name mismatch.", "Ivan", ReflectionUtil.getDeepValue(userIvan, "name"));
        assertEquals("userIvan.login mismatch.", "TheBest", ReflectionUtil.getDeepValue(userIvan, "login"));
        assertEquals("userIvan.password mismatch.", "12345", ReflectionUtil.getDeepValue(userIvan, "password"));

        assertEquals(
                "personMike.firstFriend mismatch.",
                personMax,
                ReflectionUtil.getDeepValue(personMike, "firstFriend")
        );

        assertEquals(
                "personMike.firstFriend.name mismatch.",
                "Max",
                ReflectionUtil.getDeepValue(personMike, "firstFriend.name")
        );

        assertEquals(
                "personMike.friendByName.Max.id mismatch.",
                2L,
                ReflectionUtil.getDeepValue(personMike, "friendByName.Max.id")
        );

        assertEquals(
                "personMax.friendByName.Ivan.firstFriend.friendByName.Ivan.handle mismatch.",
                "TheBest",
                ReflectionUtil.getDeepValue(personMax, "friendByName.Ivan.firstFriend.friendByName.Ivan.handle")
        );

        assertEquals(
                "personMax.friendByName.Mike.firstFriend.friendByName.Ivan.admin mismatch.",
                false,
                ReflectionUtil.getDeepValue(personMax, "friendByName.Mike.firstFriend.friendByName.Ivan.admin")
        );

        assertEquals(
                "personMike.friendByName.Mike mismatch.",
                null,
                ReflectionUtil.getDeepValue(personMike, "friendByName.Mike")
        );

        assertEquals(
                "personMax.friendByName.Mike.firstFriend.friendList.-1.friendSet.0.friendSet.-2.name mismatch.",
                "Max",
                ReflectionUtil.getDeepValue(
                        personMax, "friendByName.Mike.firstFriend.friendList.-1.friendSet.0.friendSet.-2.name"
                )
        );

        String longQueryCycle = "friendByName.Mike.friendSet.-1.friendList.-1.friendList.1.friendSet.1";
        int longQueryCycleCount = 100000;

        StringBuilder longQuery = new StringBuilder(
                longQueryCycleCount * longQueryCycle.length() + (longQueryCycleCount - 1) * ".".length()
        ).append(longQueryCycle);

        for (int i = 1; i < longQueryCycleCount; ++i) {
            longQuery.append('.').append(longQueryCycle);
        }

        assertEquals(
                String.format("personMax[.%s x%d].age mismatch.", longQueryCycle, longQueryCycleCount),
                21,
                ReflectionUtil.getDeepValue(personMax, longQuery + ".age")
        );
    }

    private static class Person {
        protected final long id;
        protected final int age;
        protected final String name;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        protected final Map<String, Person> friendByName = new LinkedHashMap<>();
        protected final List<Person> friendList = new ArrayList<>();
        protected final Collection<Person> friendSet = new LinkedHashSet<>();

        private Person(long id, int age, String name) {
            this.id = id;
            this.age = age;
            this.name = name;
        }

        public void addFriend(Person friend) {
            friendByName.put(friend.name, friend);
            friendList.add(friend);
            friendSet.add(friend);
        }

        public Person getFirstFriend() {
            return friendByName.isEmpty() ? null : friendByName.values().iterator().next();
        }

        @Override
        public String toString() {
            return StringUtil.toString(this, false, "id", "age", "name");
        }
    }

    private static class User extends Person {
        protected final String login;
        protected final String password;

        private User(long id, int age, String name, String login, String password) {
            super(id, age, name);
            this.login = login;
            this.password = password;
        }

        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(login);
        }

        public String handle() {
            return login;
        }
    }
}
