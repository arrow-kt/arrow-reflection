package arrow.meta.samples

import kotlin.reflect.KClass

data class Street(val number: Int, val name: String)

data class Address(val city: String, val street: Street)

data class Company(val name: String, val address: Address, val employees: List<Employee>)

data class Employee(val name: String, val company: Company?)

val john = Employee(
  "John Doe", company = Company(
    "Kategory", Address(
      "Functional city", Street(
        42, "lambda street"
      )
    )
  )
)


interface Path<Root : Any, Out> {
  fun get(instance: Root): Out
  fun set(instance: Root, value: Out): Root
}

interface NullablePath<Root : Any, Out> {
  fun get(instance: Root): Out?
  fun set(instance: Root, value: Out): Root
}

interface IterablePath<Root : Any, Out> {
  fun get(instance: Root): List<Out>
  fun set(instance: Root, value: Out): Root
}

operator fun <A : Any, B> KClass<A>.invoke(f: A.() -> B): Path<A, B> = TODO("synth")

private class Path_Employee_company_address_street_number : NullablePath<Employee, Int> {
  override fun get(instance: Employee): Int? = instance.company?.address?.street?.number
  override fun set(instance: Employee, value: Int): Employee = instance.copy(
    company = instance.company?.copy(
      address = instance.company.address.copy(
        street = instance.company.address.street.copy(number = value)
      )
    )
  )
}

val x = object : arrow.meta.samples.NullablePath<Employee, Int> {
  override fun get(instance: Employee): kotlin.Int? =
    instance.company?.address?.street?.number

  override fun set(instance: Employee, value: Int): Employee =
    instance.copy(company = company.copy(address = company.address.copy(street = company.address.street.copy(number = value))))
}

val <A : Any> KClass<A>.path: A get() = TODO()

fun main() {
  // Scope at compile time only accepts chains of a FirCall
  val path = @Log Employee::class.path.company?.employees?.mapNotNull { it.company?.address?.street?.number }
  // compiler plugin replaces expression above by the one below where it has generated a class capturing
  // the properties
  val path2 = Path_Employee_company_address_street_number()
  val johnUpdated = path2.set(john, 47)
  println(johnUpdated)
  //Employee(name=John Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=47, name=lambda street))))
}

