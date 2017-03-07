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
- delete reamModels in cascading mode, more testing required
- support also for deleting methods found in realmResults, and realmLists, mroe testing required

What is coming in the next phase
- support realmQuery.*asc() methods
- realmResult.addEventListener()
- realmResult.asObservable()

What is coming after
- A chart with what's being covered and what is not from RealmModel, RealmObject, RealmQuery, RealmResults, and RealmList