# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

What has been done so far
- Supporting Realm 2.3
- Supporting Robolectric 3.3.1
- Works with Mockito 1.10.19
- Works with PowerMockito 1.6.4
- Have Realm.getDefaultInstance()
- Query based on a limited number of RealmQuery methods such as equalsTo, greaterThan, lessThan, contains, endsWith
- Chaining queries
- Asynchronous and synchronous transactions with RxJava
    - Default schedulers are in main thread, feel free to update them when using Robolectric
        - RealmFactory.setTransactionScheduler(Schedulers.computation())
        - RealmFactory.setResponseScheduler(AndroidSchedulers.mainThread())
- Support or() for chaining queries
- Grouping
- Querying against realmResults
- delete reamModels in cascading mode
- support also for deleting methods found in realmResults, realmModel, realmObject and realmLists

Currently working on
- support realmQuery.*Async() methods
- realmResult.addEventListener()
- realmResult.asObservable()

What is coming after
- A chart with what's being covered and what is not from RealmModel, RealmObject, RealmQuery, RealmResults, and RealmList