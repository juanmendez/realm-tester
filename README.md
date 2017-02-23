# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

What has been done so far
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
- A chart of methods supported and tested from RealmQuery
- A chart of methods supported and tested from RealmList


What is coming after
- Go supporting more methods from RealmQuery and RealmList
- Allow to delete realmModels from their realm