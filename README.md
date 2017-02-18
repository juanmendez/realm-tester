# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, and PowerMockito.

What has been done so far
- Have Realm.getDefaultInstance()
- Keep a hold of all realmModels based on their class
- Query based on a limited number of RealmQuery methods such as equalsTo, greaterThan, lessThan, contains, endsWith
- Chaining queries (Not supporting OR yet)
- Mocking asynchronous and synchronous transactions

What is comming in the next days
- A chart of methods supported and tested from RealmQuery
- A chart of methods supported and tested from RealmList
- Enabling linking queries
- Support or() for chaining queries

What is comming in a few weeks
- Query groups
- Go supporting more methods from RealmQuery and RealmList
- Allow realmModels to delete from its realm
