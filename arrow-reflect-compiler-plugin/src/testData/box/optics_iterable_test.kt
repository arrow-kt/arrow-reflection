package foo.bar

import arrow.meta.samples.Optics
import arrow.meta.samples.invoke

data class Street(val number: Int, val name: String)

data class Address(val city: String, val street: Street)

data class Company(val name: String, val address: Address, val employees: List<Employee>)

data class Employee(val name: String, val company: Company?)

fun main() {
  val path = @Optics Employee::class { company?.employees?.mapNotNull { it.company?.address?.street?.number } }
}
