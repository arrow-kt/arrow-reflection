# Kotest behavior test

Kotest behavior test looks like:

```kotlin
class MyTests : BehaviorSpec({
  given("a broomstick") {
    `when`("I sit on it") {
       then("I should be able to fly") {
         // test code
       }
    }
        
    `when`("I throw it away") {
      then("it should come back") {
        // test code
      }
    }
  }
})
```

It has a lot of nesting. Spock framework due how Groovy works, allows to:

```groovy
def "two plus two should equal four"() {
  given:
    int left = 2
    int right = 2

  when:
    int result = left + right

  then:
    result == 4
}
```

## Proposal

```kotlin
// annotated internally with @Given
Given {
  val left = 2
  val right = 2
}

// annotated internally with @When
When {
  // `left` and `right` are virtually generated as 
  // `When` should be able to access to `Given` body params
  val result = left + right
}

// annotated internally with @Then
Then {
  // `result` is virtually generated as 
  // `Then` should be able to access to `When` body params
  result shouldBe 42
}
```
