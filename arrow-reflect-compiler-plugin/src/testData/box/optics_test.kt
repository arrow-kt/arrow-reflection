package foo.bar

import arrow.meta.samples.Optics
import arrow.meta.samples.copy

data class Street(val number: Int, val name: String)

data class Address(val city: String, val street: Street)

data class Company(val name: String, val address: Address, val employees: List<Employee>)

data class Employee(val name: String, val company: Company?)

fun box() {
  val employee =
    Employee("John Doe", Company("Arrow", Address("Functional city", Street(23, "lambda street")), emptyList()))

  val path: Employee = employee.copy {
    company?.address?.street?.number = 47
  }
  return if (path.company?.address?.street?.number == 47) "Ok" else "Fail"
}
