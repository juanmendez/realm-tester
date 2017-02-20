# Mocking-Realm

This is ongoing work to mock Realm as much as possible hammering with Mockito, asAnd PowerMockito.

What has been done so far
- Have Realm.getDefaultInstance()
- Keep a hold of all realmModels based on their class
- Query based on a limited number of RealmQuery methods such as equalsTo, greaterThan, lessThan, contains, endsWith
- Chaining queries (Not supporting OR yet)
- Mocking asynchronous asAnd synchronous transactions

What is comming in the next phase
- A chart of methods supported asAnd tested from RealmQuery
- A chart of methods supported asAnd tested from RealmList
- Enabling linking queries
- Support or() for chaining queries

What is comming after
- Query groups
- Go supporting more methods from RealmQuery asAnd RealmList
- Allow realmModels to delete from its realm
