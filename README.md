# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

[Wiki has a basic instructions how to import and use this library](https://github.com/juanmendez/Mocking-Realm/wiki)

What has been done so far
- Working with Realm 3.0.0
- Supporting Robolectric 3.3.1
- Works with Mockito 1.10.19
- Works with PowerMockito 1.6.4
- Have Realm.getDefaultInstance()
- If desired, do realm configurations in a dependency class rather than an Android component see [wiki](https://github.com/juanmendez/Mocking-Realm/wiki/How-to-initialize-Realm-when-testing).
- Querying works for around 70% of all methods, more to come in the next phase.
- Chaining queries
- Asynchronous and synchronous transactions with RxJava
    - Schedulers for testing use Schedulers.immediate()
- Support or() for chaining queries
- Grouping
- Querying against realmResults
- delete reamModels in cascading mode
- support also for deleting methods found in realmResults, realmModel, realmObject and realmLists
- support realmQuery.*Async() methods
- realmResults.addChangeListener(), realmObject.addChangeListener()
- realmResults.asObservable(),realmObject.asObservable(), not supporting realm.asObservable() at this time
- Several features not covered will simply pass, and not perform anything. In this situation, I am going to include a console message with prefix #Mocking-Realm and let you know when that occurs. The same rule will apply for features which I cover partially. For example during mocking Realm I couldn't track their annotations, due to their retention type. So I am just going to let you know to make sure include @Index in your attributes when required to use realmQuery.distinct(*) for instance.

What is coming next
- Cover RealmQuery.not()
- Finally a chart with what is covered, and what will be mocked to be ignored.
