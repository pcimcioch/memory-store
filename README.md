# Memory Store
This library was created to store arrays of a well-structured data in memory in very optimal way.

Imagine you need to store in memory millions of records. Each record stores dozens of features. You may want to model it 
as list/array of java object:
```java
public class Road {
    private final String name;
    private final int id;
    private final SurfaceType surface;
    private final short averageSpeed;
    private final boolean accessibleByCar;
    private final boolean accessibleByBike;
    private final boolean accessibleByBike;
    private final boolean accessibleByFoot;
}
```
But such object in java takes a lot of memory. Maybe SurfaceType contains only 6 possible values. It could be effectively 
stored on 3 bits, but as an enum it takes at least 32 bits. Each boolean takes 8 bits even though it could be stored in one.
Maybe averageSpeed has values from 0-280. Too much to store in one byte, so short is used. It takes 16 bits, even though
only 9 bits could be used. Moreover, each Road is a java object, which is additional few bytes of the memory overhead.

This library allows you to model the same object in more memory-efficient way. Java object references 
(like String from this example) will be represented in the same way as in objects. 
But each type that can be easily represented as the number (all numeric types, booleans and enums)
will be stored on as few bits as necessary. Also, each record will not have any additional memory overhead.

From technical point of view, all numeric data is stored in `int[][]` blocks of memory and is 32-bit padded.
All object type data is stored in `Object[][]` blocks of memory.

## Quick Start
```java
// define table structure
BitHeader<LongEncoder> accountCreationHeader = long64("accountCreation");
BitHeader<UnsignedIntegerEncoder> idHeader = Headers.unsignedIntMaxValue("personalIdentificationNumber", 999999);
BitHeader<EnumEncoder<Gender>> genderHeader = Headers.enumType("gender", Gender.class);
ObjectDirectHeader<String> nameHeader = Headers.object("name");

Table user = new Table(List.of(accountCreationHeader, idHeader, genderHeader, nameHeader));

// get encoders for each field
LongEncoder accountCreation = user.encoderFor(accountCreationHeader);
UnsignedIntegerEncoder id = user.encoderFor(idHeader);
EnumEncoder<Gender> gender = user.encoderFor(genderHeader);
ObjectDirectEncoder<String> name = user.encoderFor(nameHeader);

// use encoders to store 0th record
accountCreation.set(0, LocalDateTime.of(2000, 4, 14, 15, 22, 35).toInstant(ZoneOffset.UTC).toEpochMilli());
id.set(0, 12345);
gender.set(0, Gender.MALE);
name.set(0, "John Doe");

// use encoders to read 0th record
String message = "Our first user is " + name.get(0) + ", their account id is " + id.get(0);
```

It could be packed in more developer-friendly representation:
```java
public class UserTable {
    private final LongEncoder accountCreation;
    private final UnsignedIntegerEncoder id;
    private final EnumEncoder<Gender> gender;
    private final ObjectDirectEncoder<String> name;
    
    public UserView() {
        BitHeader<LongEncoder> accountCreationHeader = long64("accountCreation");
        BitHeader<UnsignedIntegerEncoder> idHeader = Headers.unsignedIntMaxValue("personalIdentificationNumber", 999999);
        BitHeader<EnumEncoder<Gender>> genderHeader = Headers.enumType("gender", Gender.class);
        ObjectDirectHeader<String> nameHeader = Headers.object("name");

        Table user = new Table(List.of(accountCreationHeader, idHeader, genderHeader, nameHeader));

        this.accountCreation = user.encoderFor(accountCreationHeader);
        this.id = user.encoderFor(idHeader);
        this.gender = user.encoderFor(genderHeader);
        this.name = user.encoderFor(nameHeader);
    }
    
    public String getName(int userIndex) {
        return name.get(userIndex);
    }

    public void setName(int userIndex, String name) {
        this.name.set(userIndex, name);
    }
    
    public int getId(int userIndex) {
        return id.get(userIndex);
    }

    public void setId(int userIndex, int id) {
        this.id.set(userIndex, id);
    }

    public Gender getGender(int userIndex) {
        return gender.get(userIndex);
    }

    public void setGender(int userIndex, Gender gender) {
        this.gender.set(userIndex, gender);
    }

    public Instant getAccountCreation(int userIndex) {
        return Instant.ofEpochMilli(accountCreation.get(userIndex));
    }

    public void setAccountCreation(int userIndex, Instant accountCreation) {
        this.accountCreation.set(userIndex, accountCreation.toEpochMilli());
    }
}
```

Or maybe more repository-style model if you are ok with overhead caused by constructing and deconstructing lots of objects

```java
import java.time.Instant;

public record User(
        String name,
        int id,
        Gender gender,
        Instant accountCreation
) {
}

public class UserRepository {
    private final LongEncoder accountCreation;
    private final UnsignedIntegerEncoder id;
    private final EnumEncoder<Gender> gender;
    private final ObjectDirectEncoder<String> name;

    public UserRepository() {
        BitHeader<LongEncoder> accountCreationHeader = long64("accountCreation");
        BitHeader<UnsignedIntegerEncoder> idHeader = Headers.unsignedIntMaxValue("personalIdentificationNumber", 999999);
        BitHeader<EnumEncoder<Gender>> genderHeader = Headers.enumType("gender", Gender.class);
        ObjectDirectHeader<String> nameHeader = Headers.object("name");

        Table user = new Table(List.of(accountCreationHeader, idHeader, genderHeader, nameHeader));

        this.accountCreation = user.encoderFor(accountCreationHeader);
        this.id = user.encoderFor(idHeader);
        this.gender = user.encoderFor(genderHeader);
        this.name = user.encoderFor(nameHeader);
    }

    public User getUser(int userIndex) {
        return new User(
                name.get(userIndex),
                id.get(userIndex),
                gender.get(userIndex),
                Instant.ofEpochMilli(accountCreation.get(userIndex))
        );
    }
    
    public void setUser(int userIndex, User user) {
        name.set(userIndex, user.name());
        id.set(userIndex, user.id());
        gender.set(userIndex, user.gender());
        accountCreation.set(userIndex, user.accountCreation().toEpochMilli());
    }
}
```

## Maven Repository
TODO release
Library is available in Central Maven Repository

```xml
<dependency>
  <groupId>com.github.pcimcioch</groupId>
  <artifactId>memory-store</artifactId>
  <version>1.0.0</version>
</dependency>
```

```kotlin
implementation("com.github.pcimcioch:memory-store:1.0.0")
```

## Similar Solutions
In Java there are similar existing solutions that are somehow related to this library functionality.

First is [Foreign-Memory Access API](https://openjdk.java.net/jeps/393) which is a way for java application to access
host memory directly. It provides and API to define MemoryLayouts similar to what this library provides. It may be a better
choice for your use case.
As of January 2022 this feature is incubating as part of Project Panama.

Additionally, there is a work in progress to introduce [Primitive Classes](https://openjdk.java.net/jeps/401) in Java
that would not contain class headers, so each primitive object would not have any memory overhead. 
As of January 2022 this feature is still a candidate as part of Project Valhalla, and it is not known when it will be implemented.
When fully implemented, it may solve few use-cases that this library solves.

## Performance
There are some simple JMH benchmarks to test performance of this solution.
Two approaches were compared: reading and writing field values using `Table` and using just array of Java objects.

Benchmark results:

|       | Table     | Object     |
|-------|-----------|------------|
| Read  | 56 ops/us | 105 ops/us |
| Write | 88 ops/us | 447 ops/us |

Writes using Tables are about 5 times slower.

Reads using Tables are about twice as slow.

NOTE: JMH benchmarks are mostly demonstrative. Results may vary depending on your environment and specs. There are used
only to demonstrate approximate performance differences. To run those benchmarks locally, call
```
./gradlew jmh
```

## Features
This library allows creating Tables with unlimited (well, memory limited) number of Records.

Each Record has a static number of fields.

Field can be a binary field stored on arbitrary number of bits, or object field that stores java reference.

Field types are described by `Headers`. There are multiple predefined `Headers` to store different data types

### Table
Table can be created using `Table` class:
```java
Table test = new Table(List.of(
    Headers.long64("dateOfBirth"),                                          // date of birth stored on 64 bits
    Headers.unsignedIntMaxValue("personalIdentificationNumber", 999999),    // id stored on as many bits as required to store values in range 0-999999
    Headers.enumType("gender", Gender.class),                               // gender stored on as many bits as required to store all values of Geder enum
    Headers.<String>object("name")                                          // name stored as an String object
));
```

### Primitive types
All java primitive values can be represented using:
```java
Headers.bool("headerName");
Headers.byte8("headerName");
Headers.char16("headerName");
Headers.double64("headerName");
Headers.float32("headerName");
Headers.int32("headerName");
Headers.long64("headerName");
Headers.short16("headerName");
```

### Dynamic Size Integers
To represent unsigned int values stored on defined number of bits use
```java
Headers.unsignedIntOnBits("headerName", 3); // store unsigned integer on 3 bits, so the values are in range [0, 7]
Headers.unsignedIntMaxValue("headerName", 120); // store unsigned integer on 7 bits, as it's the lowest number of bits that can be used to store values [0, 120]. It may allow storing more values. In this case it will really allow storing [0, 127]
```

To represent signed int values on defined number of bits use
```java
Headers.intOnBits("headerName", -2, 3); // store signed integer on 3 bits with minimum value -2, so in range [-2, 5]
Headers.intRange("headerName", -2, 10); // store signed integer on 4 bits, as it's the lowest number of bits that can be used to store values [-2, 10]. It may allow storing more values. In this case it will really allow storing [-2, 13]
Headers.intRange("headerName", 2, 8); // minimum value doesn't need to be negative
```

### Enums
To store enum values
```java
Headers.enumType("headerName", MyEnum.class); // store all possible non-null values of MyEnum enum on lowest possible number of bits
Headers.nullableEnumType("headerName", MyEnum.class); // store all possible values of MyEnum enum, including null, on lowest possible number of bits
Headers.enumTypeOnBits("headerName", 3, enumFactory, enumIndexer); // store enum on given number of bits. In this case 3 bits, so 8 different values can be stored. enumFactory and enumIndexer are used to translate enum value to signed integer that will be stored on those bits, and back
Headers.enumTypeMaxSize("headerName", 12, enumFactory, enumIndexer); // store given number of possible enum values. In this case 12 different values will be stored on 4 bits. In reality this will be rounded up to 16 values, as 4 bits can store 16 different states
```

### Objects
To store java objects use
```java
Headers.<String>object("headerName"); // store String objects
```
This allows storing null values

### Object Pools
Sometimes, each of your Records need to reference some other object. But many records reference the same object.
For memory optimization, the same objects could be just one object in the memory.
To represent such relation in the Table, first create object pool:
```java
PoolDefinition pool = Headers.poolOnBits("poolName", 5); // Pool of different objects. 5 bits indicate that this pool will be able to store 2^5=32 different objects
PoolDefinition pool = Headers.poolSize("poolName", 30); // Pool of 30 different objects. It will use 5 bits to store object indexes. In reality, it will allow storing 32 different objects, as 5 bits can be used to store 32 different states
```

Then, to create field storing pooled objects:
```java
Headers.<MyClass>objectPool("headerName", pool); // store MyClass objects, pooling the same instances
```

Objects will be compared using `equals` method

### BitSets
BitSets are not implemented yet

[//]: # (TODO Implement)

### EnumSets
EnumSets are not implemented yet

[//]: # (TODO Implement)

### Memory Layout
When you define multiple fields in the table, they have to be somehow lied out in the memory. By default, 
`AutomaticMemoryLayoutBuilder`will be used. It tries to automatically compute most optimal memory layout. 
If you need to define your own layout you can provide your own implementation of `MemoryLayoutBuilder`.

Custom memory layout can be provided in `Table` constructor

Two additional implementations are provided in this library.

`NonOverlappingMemoryLayoutBuilder`
```java
new NonOverlappingMemoryLayoutBuilder(
    32,     // word size of 32 bits. It's default value. Table supports only 32 bit words
    4,      // 4 words per record
    Map.of(
        Headers.int32("header1"), new MemoryPosition(0, 0),     // 32 bit header located at 0th word in record, starting on 0th bit. It will take one whole word
        Headers.long64("header2"), new MemoryPosition(1, 0),    // 64 bit header located at 1st word in record, starting on 0th bit. It will take two whole words
        Headers.byte8("header3"), new MemoryPosition(3, 0),     // 8 bit header located at 3rd word in record, starting on 0th bit. It will take 1/4th of the record
        Headers.short16("header4"), new MemoryPosition(3, 8)    // 16 bit header located at 3rd word in record, starting on 8th bit. It will take half of the record
    )
);
```

In this example last 8 bits of the 3rd word in the record will not be used - it will be a padding. As builder name suggests,
headers cannot overlap on each another. Constructor will throw na exception if such situation occurs.

`OverlappingMemoryLayoutBuilder`
```java
new OverlappingMemoryLayoutBuilder(
    32,     // word size of 32 bits. It's default value. Table supports only 32 bit words
    4,      // 4 words per record
    Map.of(
        Headers.int32("header1"), new MemoryPosition(0, 0),     // 32 bit header located at 0th word in record, starting on 0th bit. It will take one whole word
        Headers.long64("header2"), new MemoryPosition(1, 0),    // 64 bit header located at 1st word in record, starting on 0th bit. It will take two whole words
        Headers.byte8("header3"), new MemoryPosition(3, 0),     // 8 bit header located at 3rd word in record, starting on 0th bit. It will take 1/4th of the record
        Headers.short16("header4"), new MemoryPosition(3, 0)    // 16 bit header located at 3rd word in record, starting on 0th bit. It will take half of the record
    )
);
```
This variant of a builder allows headers to overlap. In this example, `header3` and `header4` overlap and take the same part of the memory.
Such approach can be used to construct unions to store in the same part of memory one of many type of data.

### Unions
Union is a type of data structure that holds one of other data structure. For example that's how they look like in 
[C++](https://en.cppreference.com/w/cpp/language/union). This library allows declaring unions using `OverlappingMemoryLayoutBuilder`.
See [Memory Layout](#memory-layout) section for more details

### Persistence
Persistence is not yet implemented. The very next features to be added are storing and loading Tables in files and input/output stream in binary format.

[//]: # (TODO Implement)