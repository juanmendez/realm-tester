# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

What has been done so far
- Supporting Realm 2.3
- Have Realm.getDefaultInstance()
- Query based on a limited number of RealmQuery methods such as equalsTo, greaterThan, lessThan, contains, endsWith
- Chaining queries
- Asynchronous and synchronous transactions with RxJava
    - Default schedulers are in main thread, feel free to update them for Robolectric
        - RealmFactory.setTransactionScheduler(Schedulers.computation())
        - RealmFactory.setResponseScheduler(AndroidSchedulers.mainThread())
- Support or() for chaining queries
- Grouping works, but needs more testing
- Querying against realmResults
- Testing in PowerMock, and Robolectric 3.0 + PowerMock at the moment

What is coming in the next phase
- delete reamModels in cascading mode
    - This has been covered, yet needs more polishing and testing
- support also for deleting methods found in realmResults, and realmLists
    - This has been covered, yet needs more polishing and testing

What is coming after
- A chart with what's being covered and what is not from RealmModel, RealmObject, RealmQuery, RealmResults, and RealmList

What is not intended to cover during this phase
- mocking event listeners for realmResults
- realmQuery.findAllAsync is going to work synchronously like findAll method