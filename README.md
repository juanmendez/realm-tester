# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

[Wiki has a basic instructions how to import and use this library](https://github.com/juanmendez/Mocking-Realm/wiki)

What has been done so far
- Supporting Realm 2.3
- Supporting Robolectric 3.3.1
- Works with Mockito 1.10.19
- Works with PowerMockito 1.6.4
- Have Realm.getDefaultInstance()
- If desired, do realm configurations in a dependency class rather than an Android component see [wiki](https://github.com/juanmendez/Mocking-Realm/wiki/How-to-initialize-Realm-when-testing).
- Query based on a limited number of RealmQuery methods such as equalsTo, greaterThan, lessThan, contains, endsWith
- Chaining queries
- Asynchronous and synchronous transactions with RxJava
    - Schedulers for testing use Schedulers.immediate()
- Support or() for chaining queries
- Grouping
- Querying against realmResults
- delete reamModels in cascading mode
- support also for deleting methods found in realmResults, realmModel, realmObject and realmLists
- support realmQuery.*Async() methods
- realmResult.addEventListener()
- realmResult.asObservable()

What is coming next
- A chart with what's being covered and what is not from RealmModel, RealmObject, RealmQuery, RealmResults, and RealmList