package com.codeforces.commons.reflection;

import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.text.StringUtil;
import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
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

    public void testCopyProperties() throws InvocationTargetException, IllegalAccessException {
        for (int t = 0; t <= 20; t++) {
            int count = 10000;
            B[] b = new B[count];
            for (int i = 0; i < count; i++) {
                b[i] = newB();
            }
            C[] bb = new C[count];

            for (int i = 0; i < count; i++) {
                bb[i] = new C();
            }

            long start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                //BeanUtils.copyProperties(b[i], bb[i]);
                ReflectionUtil.copyProperties(b[i], bb[i]);
            }
            //System.out.println(System.currentTimeMillis() - start);

            for (int i = 0; i < count; i++) {
                assertEquals(b[i], bb[i]);
            }
        }
    }

    public B newB() {
        B b = new B();
        b.setId(RandomUtil.getRandomLong());
        b.setSize(RandomUtil.getRandomInt(10));
        if (RandomUtil.getRandomInt(2) == 0) {
            b.setName(RandomUtil.getRandomToken());
        }
        b.setVolume(RandomUtil.getRandomLong() * 1.0 / RandomUtil.getRandomLong());
        if (RandomUtil.getRandomInt(2) == 0) {
            b.setType(A.Type.values()[RandomUtil.getRandomInt(A.Type.values().length)]);
        }
        b.setLetter((char) ('a' + RandomUtil.getRandomInt(26)));
        if (RandomUtil.getRandomInt(2) == 0) {
            b.setKind(B.Kind.values()[RandomUtil.getRandomInt(B.Kind.values().length)]);
        }
        if (RandomUtil.getRandomInt(2) == 0) {
            BB nestedInstance = new BB();
            nestedInstance.setUuid(RandomUtil.getRandomLong());
            b.setNestedInstance(nestedInstance);
        }

        return b;
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

    public static class A {
        private long id;
        private String name;
        private int size;
        private double volume;
        private Type type;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public enum Type {
            LARGE,
            SMALL
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;

            A a = (A) o;

            if (id != a.id) return false;
            if (size != a.size) return false;
            if (Double.compare(a.volume, volume) != 0) return false;
            if (name != null ? !name.equals(a.name) : a.name != null) return false;
            return type == a.type;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (int) (id ^ (id >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + size;
            temp = Double.doubleToLongBits(volume);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }

    public static class C extends A {
        private char letter;
        private Kind kind;
        private AA nestedInstance;

        public AA getNestedInstance() {
            return nestedInstance;
        }

        public void setNestedInstance(AA nestedInstance) {
            this.nestedInstance = nestedInstance;
        }


        public char getLetter() {
            return letter;
        }

        public void setLetter(char letter) {
            this.letter = letter;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public enum Kind {
            X, x, Y, y
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            B b = (B) o;

            if (letter != b.letter) return false;
            if (kind == null && b.kind == null) {
                return true;
            }
            if (kind == null || b.kind == null) {
                return false;
            }
            return kind.toString().equals(b.kind.toString());
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) letter;
            result = 31 * result + (kind != null ? kind.hashCode() : 0);
            return result;
        }
    }


    public static class B extends A {
        private char letter;
        private Kind kind;
        private BB nestedInstance;

        public BB getNestedInstance() {
            return nestedInstance;
        }

        public void setNestedInstance(BB nestedInstance) {
            this.nestedInstance = nestedInstance;
        }

        public char getLetter() {
            return letter;
        }

        public void setLetter(char letter) {
            this.letter = letter;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public enum Kind {
            X, x, Y, y
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            //if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            C b = (C) o;

            if (letter != b.letter) return false;
            if (!(kind == null && b.kind == null)) {
                if (kind == null || b.kind == null) {
                    return false;
                }

                if (!kind.toString().equals(b.kind.toString())) {
                    return false;
                }
            }

            if (nestedInstance == null && b.nestedInstance == null) {
                return true;
            }
            if (nestedInstance == null || b.nestedInstance == null) {
                return false;
            }
            return nestedInstance.uuid == b.nestedInstance.uuid;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) letter;
            result = 31 * result + (kind != null ? kind.hashCode() : 0);
            result = 31 * result + (nestedInstance != null ? nestedInstance.hashCode() : 0);
            return result;
        }
    }

    public static class AA {
        private long uuid;

        public long getUuid() {
            return uuid;
        }

        public void setUuid(long uuid) {
            this.uuid = uuid;
        }
    }

    public static class BB {
        public long uuid;

        public long getUuid() {
            return uuid;
        }

        public void setUuid(long uuid) {
            this.uuid = uuid;
        }
    }
}
