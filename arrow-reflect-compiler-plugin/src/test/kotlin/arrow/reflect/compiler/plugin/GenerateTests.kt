package arrow.reflect.compiler.plugin

import arrow.reflect.compiler.plugin.runners.AbstractBoxTest
import arrow.reflect.compiler.plugin.runners.AbstractDiagnosticTest
import arrow.reflect.compiler.plugin.runners.AbstractTransformationTest
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
  generateTestGroupSuiteWithJUnit5 {
    testGroup(testDataRoot = "src/testData", testsRoot = "src/testGenerated") {
      testClass<AbstractDiagnosticTest> {
        model("diagnostics")
      }

      testClass<AbstractBoxTest> {
        model("box")
      }

      testClass<AbstractTransformationTest> {
        model("transformation")
      }
    }
  }
}
