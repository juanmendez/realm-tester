package info.juanmendez.mockrealmdemo;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;

import info.juanmendez.mockrealm.MockRealm;
import info.juanmendez.mockrealm.models.RealmAnnotation;
import info.juanmendez.mockrealm.test.MockRealmTester;
import info.juanmendez.mockrealmdemo.models.Dog;
import info.juanmendez.mockrealmdemo.models.Person;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmException;
import rx.Observable;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by @juanmendezinfo on 2/10/2017.
 */
public class QueryTests extends MockRealmTester {
    Realm realm;

    @Before
    public void before() throws Exception {
        MockRealm.prepare();

        /**
         * We need now to specify each class having realm annotations
         */
        MockRealm.addAnnotations(RealmAnnotation.build(Dog.class)
                        .primaryField("id")
                        .indexedFields("name", "age", "birthdate", "nickname"),
                RealmAnnotation.build(Person.class)
                        .primaryField("id")
                        .indexedFields("name"));

        realm = Realm.getDefaultInstance();
    }

    @Test
    public void shouldCreateObject() {
        MockRealm.clearData();
        assertNotNull(realm.createObject(Dog.class));
    }

    /**
     * Realm mocked is suppose to bounce back same object to copyToRealm
     *
     * @throws Exception
     */
    @Test
    public void shouldCopyToRealm() throws Exception {
        MockRealm.clearData();
        Dog dog = new Dog();
        dog.setName("Max");
        dog.setAge(1);

        realm.copyToRealm(dog);

        assertEquals("There is one dog", realm.where(Dog.class).count(), 1);
    }

    /**
     * So lets see if I can prepare an object, and I can get a realmResult back, and check the size to be equal to 1
     */
    @Test
    public void shouldExecuteTransaction() {
        MockRealm.clearData();

        realm.executeTransaction(realm1 -> {
            Dog dog = realm.createObject(Dog.class);
            dog.setAge(1);
            dog.setName("Max");
            dog.setBirthdate(new Date(2011, 6, 10));
        });

        assertEquals("there is now one element available", realm.where(Dog.class).count(), 1);
    }

    @Test
    public void shouldUpdate() {

        //@Before specifies primary, and indexed fields in Dog class
        MockRealm.clearData();

        Dog dog;

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setId(1);
        dog.setAge(1);
        dog.setName("Max");
        dog.setBirthdate(new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setId(2);
        dog.setAge(2);
        dog.setName("Rex");
        dog.setBirthdate(new Date(2016, 6, 10));
        realm.commitTransaction();

        realm.beginTransaction();
        dog = new Dog();
        dog.setId(1);
        dog.setNickname("Maximilian");
        realm.copyToRealm(dog);
        realm.commitTransaction();

        assertEquals("There are two dogs after updating one", realm.where(Dog.class).count(), 2);
        assertEquals("Our updated dog has a new nickname", realm.where(Dog.class).equalTo("id", 1).findFirst().getNickname(), "Maximilian");

    }

    /**
     * assures we can get back dogs who were born after 2009.
     */
    @Test
    public void shouldQueryByConditions() {
        MockRealm.clearData();

        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Max");
        dog.setBirthdate(new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Rex");
        dog.setBirthdate(new Date(2016, 6, 10));


        RealmResults<Dog> dogs = realm.where(Dog.class).greaterThanOrEqualTo("birthdate", new Date(2009, 6, 10)).findAll();
        assertNotNull("dog is found", dogs);

        //iteration is working
        for (Dog _dog : dogs) {
            System.out.println("dog: " + _dog.getName());
        }

        //between
        dogs = realm.where(Dog.class).between("birthdate", new Date(2016, 6, 10), new Date(2017, 0, 1)).findAll();

        assertEquals("There is only one dog born between during or after 07/10/2016, and 01/01/2017", dogs.size(), 1);
    }

    @Test
    public void shouldQueryByCaseSensitivity() {

        MockRealm.clearData();

        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));

        RealmResults<Dog> dogs = realm.where(Dog.class).contains("name", "id", Case.INSENSITIVE).findAll();
        assertEquals("Two dogs contain 'id' cased-insensitive", dogs.size(), 2);


        dogs = realm.where(Dog.class).contains("name", "id").findAll();
        assertEquals("One dog contains 'id' cased sensitive", dogs.size(), 1);


        dogs = realm.where(Dog.class).endsWith("name", "dez", Case.SENSITIVE).findAll();
        assertEquals("Two dogs end with 'dez' case-sensitive", dogs.size(), 2);
    }

    @Test
    public void shouldCount() {
        MockRealm.clearData();

        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));

        long numOfDogs = realm.where(Dog.class).endsWith("name", "dez", Case.SENSITIVE).count();
        assertEquals("Two dogs end with 'dez' case-sensitive", numOfDogs, 2);
    }

    @Test
    public void shouldDoLinkingQueries() {
        MockRealm.clearData();

        Person person;
        Dog dog;

        //person 1
        person = realm.createObject(Person.class);
        person.setName("Pete");

        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));
        person.getDogs().add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));
        person.getDogs().add(dog);


        //person 2
        person = realm.createObject(Person.class);
        person.setName("Roger");

        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Idalgo Martinez");
        dog.setBirthdate(new Date(2011, 6, 10));
        person.getDogs().add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Flores");
        dog.setBirthdate(new Date(2016, 6, 10));
        person.getDogs().add(dog);

        RealmResults<Person> people = realm.where(Person.class).contains("dogs.name", "Flores").findAll();
        assertEquals("there is one person found with such dog", 1, people.size());
    }

    @Test
    public void shouldQueryByOr() {

        MockRealm.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2012, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2014, 6, 10));


        RealmResults<Dog> dogs = realm.where(Dog.class).contains("name", "Mendez").or().contains("name", "Fernandez").findAll();
        assertEquals("There are three dogs with those last names", dogs.size(), 3);

        //lets do the same criteria but this time from the three dogs, lets find the ones born before the date
        dogs = realm.where(Dog.class).contains("name", "Mendez").or().contains("name", "Fernandez")
                .beginGroup().lessThan("birthdate", new Date(2013, 0, 1)).endGroup().findAll();

        //Idalgo and Hernan were born before 2013
        assertEquals("There are two dogs born before the given date", dogs.size(), 2);
    }

    @Test
    public void shouldQueryAgainstRealmResults() {

        MockRealm.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2012, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2014, 6, 10));

        RealmResults<Dog> dogs = realm.where(Dog.class).contains("name", "Mendez").or().contains("name", "Fernandez").findAll();

        //rather than previous test, lets skip grouping and query against dogs
        dogs = dogs.where().lessThan("birthdate", new Date(2013, 0, 1)).findAll();

        assertEquals("There are two dogs born before the given date", dogs.size(), 2);
    }

    @Test
    public void shouldDeleteRealmObject() {
        MockRealm.clearData();

        realm.where(Person.class).findFirstAsync().addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel element) {

            }
        });

        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        RealmList list = new RealmList();
        Dog dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Max");
        dog.setBirthdate(new Date(2011, 6, 10));
        list.add(dog);
        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Rex");
        dog.setBirthdate(new Date(2016, 6, 10));
        list.add(dog);
        person.setDogs(list);
        person.setFavoriteDog(dog);
        realm.commitTransaction();

        realm.beginTransaction();
        dog.deleteFromRealm();
        realm.commitTransaction();

        realm.beginTransaction();
        realm.where(Dog.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals("There is one dog left", realm.where(Dog.class).count(), 0);
        assertNull("Person's favorite dog is gone", person.getFavoriteDog());
        assertEquals("There is also one dog in the list", person.getDogs().size(), 0);
    }

    @Test
    public void findModelRelationship() {

        MockRealm.clearData();

        Person person = realm.createObject(Person.class);
        person.setDogs(new RealmList<>());

        Set<Field> fieldSet = Whitebox.getAllInstanceFields(person);

        for (Field field : fieldSet) {

            if (RealmModel.class.isAssignableFrom(field.getType())) {
                System.out.println("we will watch for " + field.getName());
            }
        }
    }

    @Test
    public void shouldFilterByPersonClass() {

        MockRealm.clearData();

        RealmList list = new RealmList(new Dog(), new Dog(), new Dog());
        list.add(new Dog());
        list.add(new Person());
        list.add(new Dog());
        list.add(new Dog());
        list.add(new Dog());
        list.add(new Dog());
        list.add(new Person());
        list.add(new Person());
        list.add(new Person());
        list.add(new Person());
        list.add(new Person());

        Observable.from(list)
                .ofType(Person.class)
                .subscribe(o -> {
                    System.out.println(o.getClass().getSimpleName());
                });
    }

    @Test
    public void shouldBeIn() {

        MockRealm.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2011, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2012, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2014, 6, 10));

        RealmResults<Dog> dogs = realm.where(Dog.class).in("age", new Integer[]{2, 5}).findAll();

        assertEquals("There are two dogs born before the given date", dogs.size(), 3);

        dogs = realm.where(Dog.class).in("name", new String[]{"Idalgo Mendez"}).findAll();
        assertEquals("There is one dog with that name", dogs.size(), 1);

        dogs = realm.where(Dog.class).in("name", new String[]{"IdAlgo MendEz", "HERNAN FerNandeZ"}, Case.INSENSITIVE).findAll();
        assertEquals("There are two dogs with those names", dogs.size(), 2);


        dogs = realm.where(Dog.class).in("name", new String[]{"IdAlgo MendEz", "HERNAN FerNandeZ"}).findAll();
        assertEquals("There are no dogs with names like that", dogs.size(), 0);

        dogs = realm.where(Dog.class).in("birthdate", new Date[]{new Date(2011, 6, 10), new Date(2014, 6, 10)}).findAll();
        assertEquals("There are two dogs with those names", dogs.size(), 2);
    }

    @Test
    public void shouldDoMax() {

        MockRealm.clearData();

        Dog dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo Mendez");
        dog.setBirthdate(new Date(2010, 6, 9));


        dog = realm.createObject(Dog.class);
        dog.setAge(1);
        dog.setName("Fido Fernandez");
        dog.setBirthdate(new Date(2016, 6, 10));


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan Fernandez");
        dog.setBirthdate(new Date(2015, 6, 10));

        dog = realm.createObject(Dog.class);
        dog.setAge(5);
        dog.setName("Pedro Flores");
        dog.setBirthdate(new Date(2012, 2, 1));


        assertEquals("max age is ", 6, realm.where(Dog.class).max("age").intValue());
        assertEquals("min age is ", 1, realm.where(Dog.class).min("age").intValue());
        assertEquals("average age is ", Double.valueOf((float) 3.5), Double.valueOf(realm.where(Dog.class).average("age")));
    }

    @Test
    public void shouldDoDistinct() {
        MockRealm.clearData();

        //lets do this first with realmList
        Dog dog;
        Person person;
        RealmList<Dog> dogs = new RealmList<>();
        RealmList<Person> persons = new RealmList<>();

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog idalgo = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog fido = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate(new Date(2015, 6, 10));
        Dog hernan = dog;
        dogs.add(dog);


        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate(new Date(2015, 6, 10));
        Dog hernan2 = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        Dog nully = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate(new Date(2015, 2, 1));
        Dog chibi = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate(new Date(2014, 2, 1));
        Dog andromeda = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate(new Date(2005, 2, 1));
        Dog baxter = dog;
        dogs.add(dog);

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate(new Date(2007, 2, 1));
        Dog beethoven = dog;
        dogs.add(dog);

        realm.commitTransaction();

        realm.beginTransaction();
        person = realm.createObject(Person.class);
        person.setName("Chiu-Ki");
        person.setFavoriteDog(nully);
        persons.add(person);

        person = realm.createObject(Person.class);
        person.setName("Karl");
        person.setFavoriteDog(andromeda);
        person.setDogs(new RealmList<>(beethoven, baxter, hernan, nully));
        persons.add(person);

        person = realm.createObject(Person.class);
        person.setName("Jimmy");
        person.setFavoriteDog(baxter);
        person.setDogs(new RealmList<>(chibi, andromeda, fido, baxter));
        persons.add(person);

        person = realm.createObject(Person.class);
        person.setName("Donn");
        person.setFavoriteDog(fido);
        person.setDogs(new RealmList<>(idalgo, baxter, andromeda, nully, chibi));
        persons.add(person);

        person = realm.createObject(Person.class);
        person.setName("Mark");
        person.setFavoriteDog(chibi);
        person.setDogs(new RealmList<>(chibi, nully, andromeda, baxter));
        persons.add(person);
        realm.commitTransaction();


        /*RealmList<Person> distinctPersons = new QueryDistinct().perform("name", persons );
        assertEquals("there are 7 dogs with distinct names", dogs.size(), 7 );*/

       /* dogs = realm.where(Dog.class).findAll().distinct("birthdate");
        assertEquals("out of the first 6 dogs, there are 6 whose birthdays are unique", dogs.size(), 7 );*/

        RealmResults<Dog> distincts = realm.where(Dog.class).distinct("name", "age").sort("name");

        for (Dog d : distincts) {
            System.out.println("dog: " + d.getName() + ", age: " + d.getAge());
        }
    }


    @Test
    public void shouldQuerySortGoSmooth() {
        MockRealm.clearData();

        //lets do this first with realmList
        Dog dog;
        Person person;

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog idalgo = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog fido = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate(new Date(2015, 6, 10));
        Dog hernan = dog;


        dog = realm.createObject(Dog.class);
        Dog nully = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate(new Date(2015, 2, 1));
        Dog chibi = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate(new Date(2014, 2, 1));
        Dog andromeda = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate(new Date(2005, 2, 1));
        Dog baxter = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate(new Date(2007, 2, 1));
        Dog beethoven = dog;
        realm.commitTransaction();

        realm.beginTransaction();
        person = realm.createObject(Person.class);
        person.setName("Chiu-Ki");
        person.setFavoriteDog(nully);

        person = realm.createObject(Person.class);
        person.setName("Karl");
        person.setFavoriteDog(andromeda);
        person.setDogs(new RealmList<>(beethoven, baxter, hernan, nully));


        person = realm.createObject(Person.class);
        person.setName("Jimmy");
        person.setFavoriteDog(baxter);
        person.setDogs(new RealmList<>(chibi, andromeda, fido, baxter));

        person = realm.createObject(Person.class);
        person.setName("Donn");
        person.setFavoriteDog(fido);
        person.setDogs(new RealmList<>(idalgo, baxter, andromeda, nully, chibi));

        person = realm.createObject(Person.class);
        person.setName("Mark");
        person.setFavoriteDog(chibi);
        person.setDogs(new RealmList<>(chibi, nully, andromeda, baxter));

        realm.commitTransaction();

        RealmResults<Person> unsorted = realm.where(Person.class).findAll();
        RealmResults<Person> sorted = realm.where(Person.class).findAllSortedAsync(new String[]{"favoriteDog.name", "favoriteDog.age"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        sorted.addChangeListener(elements -> {

            for (RealmModel p : elements) {
                System.out.println("");
                System.out.println(((Person) p).getName());
                System.out.println("Favorite dog: " + ((Person) p).getFavoriteDog().getName() + ", age: " + ((Person) p).getFavoriteDog().getAge());
            }
        });
    }


    @Test
    public void sholdWorkWithNot() {
        MockRealm.clearData();

        //lets do this first with realmList
        Dog dog;
        Person person;

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog idalgo = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog fido = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate(new Date(2015, 6, 10));
        Dog hernan = dog;


        dog = realm.createObject(Dog.class);
        Dog nully = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate(new Date(2015, 2, 1));
        Dog chibi = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate(new Date(2014, 2, 1));
        Dog andromeda = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate(new Date(2005, 2, 1));
        Dog baxter = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate(new Date(2007, 2, 1));
        Dog beethoven = dog;
        realm.commitTransaction();

        realm.beginTransaction();
        person = realm.createObject(Person.class);
        person.setName("Chiu-Ki");
        person.setFavoriteDog(nully);

        person = realm.createObject(Person.class);
        person.setName("Karl");
        person.setFavoriteDog(andromeda);
        person.setDogs(new RealmList<>(beethoven, baxter, hernan, nully));


        person = realm.createObject(Person.class);
        person.setName("Jimmy");
        person.setFavoriteDog(baxter);
        person.setDogs(new RealmList<>(chibi, andromeda, fido, baxter));

        person = realm.createObject(Person.class);
        person.setName("Donn");
        person.setFavoriteDog(fido);
        person.setDogs(new RealmList<>(idalgo, baxter, andromeda, nully, chibi));

        person = realm.createObject(Person.class);
        person.setName("Mark");
        person.setFavoriteDog(chibi);
        person.setDogs(new RealmList<>(chibi, nully, andromeda, baxter));
        realm.commitTransaction();

        long dogCount = realm.where(Dog.class).count();
        RealmResults<Dog> dogs = realm.where(Dog.class)
                .not()
                .beginGroup()
                .equalTo("name", "Baxter")
                .or()
                .equalTo("name", "Fido")
                .endGroup()
                .findAll();

        assertEquals("There are two dogs excluded", dogCount - 2, dogs.size());

        //Of course, with this particular query, it is probably easier to use in() --Realm
        dogs = realm.where(Dog.class)
                .not()
                .in("name", new String[]{"Fido", "Baxter"})
                .findAll();

        assertEquals("There are two dogs excluded", dogCount - 2, dogs.size());

        //find dogs whose name is null
        dogs = realm.where(Dog.class)
                .not()
                .isNull("name")
                .findAll();

        //nully has no name
        assertEquals("There is one dog excluded", dogCount - 1, dogs.size());

        //using isNotNull whichi is the same process as not().isNull()
        dogs = realm.where(Dog.class)
                .isNotNull("name")
                .findAll();
        assertEquals("There is one dog excluded", dogCount - 1, dogs.size());

        //just making sure, we find nully dog.
        dogs = realm.where(Dog.class)
                .isNull("name")
                .findAll();
        assertEquals("There is one dog excluded", 1, dogs.size());


        //find all dogs whose nane is not Fido
        dogs = realm.where(Dog.class)
                .not().equalTo("name", "Fido")
                .findAll();
        assertEquals("There is one dog excluded", dogCount - 1, dogs.size());

        //same as not().isEqualTo
        dogs = realm.where(Dog.class)
                .notEqualTo("name", "Fido")
                .findAll();
        assertEquals("There is one dog excluded", dogCount - 1, dogs.size());


        Long ownersCount = realm.where(Person.class).count();

        //find dog owners whose favorite dogs do not contain names such as Baxter of Fidl
        RealmResults<Person> owners = realm.where(Person.class).not()
                .beginGroup()
                .contains("favoriteDog.name", "Fido")
                .or()
                .contains("favoriteDog.name", "Baxter")
                .endGroup()
                .findAll();

        assertEquals("There are two owners excluded", ownersCount - 2, owners.size());


        //find owners who don't have Fido, and Baxter among their dogs realmList.
        owners = realm.where(Person.class).not()
                .beginGroup()
                .contains("dogs.name", "Fido")
                .or()
                .contains("dogs.name", "Baxter")
                .endGroup()
                .findAll();

        assertEquals("One owner doesn't have those dogs", 1, owners.size());
    }

    @Test(expected = RealmException.class)
    public void shouldEmptyWork() {

        MockRealm.clearData();

        //lets do this first with realmList
        Dog dog;
        Person person;

        realm.beginTransaction();
        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Idalgo");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog idalgo = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(6);
        dog.setName("Fido");
        dog.setBirthdate(new Date(2016, 6, 9));
        Dog fido = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Hernan");
        dog.setBirthdate(new Date(2015, 6, 10));
        Dog hernan = dog;


        dog = realm.createObject(Dog.class);
        Dog nully = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(2);
        dog.setName("Chibi");
        dog.setBirthdate(new Date(2015, 2, 1));
        Dog chibi = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(3);
        dog.setName("Andromeda");
        dog.setBirthdate(new Date(2014, 2, 1));
        Dog andromeda = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(12);
        dog.setName("Baxter");
        dog.setBirthdate(new Date(2005, 2, 1));
        Dog baxter = dog;

        dog = realm.createObject(Dog.class);
        dog.setAge(10);
        dog.setName("Beethoven");
        dog.setBirthdate(new Date(2007, 2, 1));
        Dog beethoven = dog;
        realm.commitTransaction();

        realm.beginTransaction();
        person = realm.createObject(Person.class);
        person.setName("Chiu-Ki");
        person.setFavoriteDog(nully);

        person = realm.createObject(Person.class);
        person.setName("Karl");
        person.setFavoriteDog(andromeda);
        person.setDogs(new RealmList<>(beethoven, baxter, hernan, nully));


        person = realm.createObject(Person.class);
        person.setName("Jimmy");
        person.setFavoriteDog(baxter);
        person.setDogs(new RealmList<>(chibi, andromeda, fido, baxter));

        person = realm.createObject(Person.class);
        person.setName("Donn");
        person.setFavoriteDog(fido);
        person.setDogs(new RealmList<>(idalgo, baxter, andromeda, nully, chibi));

        person = realm.createObject(Person.class);
        person.setName("Mark");
        person.setFavoriteDog(chibi);
        person.setDogs(new RealmList<>(chibi, nully, andromeda, baxter));
        realm.commitTransaction();


        Long ownersCount = realm.where(Person.class).count();
        RealmResults<Person> owners = realm.where(Person.class).isEmpty("dogs").findAll();
        //there is one owner who has no dogs.
        assertEquals("there is one dog without dogs", 1, owners.size());

        owners = realm.where(Person.class).isNotEmpty("dogs").findAll();

        //there is one owner who has no dogs.
        assertEquals("everyone has a dog except one", ownersCount - 1, owners.size());


        owners = realm.where(Person.class).not().isEmpty("dogs").findAll();

        //there is one owner who has no dogs.
        assertEquals("everyone has a dog except one", ownersCount - 1, owners.size());

        //we expect this to be an error, as isEmpty takes care of lists, strings, and binaries
        realm.where(Person.class).not().isEmpty("favoriteDog").findAll();

    }
}