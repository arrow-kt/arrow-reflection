package arrow.reflect.compiler.plugin.fir.checkers

import arrow.reflect.compiler.plugin.targets.macro.MacroCompiler
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile

class MacroFirTransformationCheckerExtension(
  session: FirSession,
  val macro: MacroInvoke
) : FirAdditionalCheckersExtension(session) {

  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
      object : FirBasicDeclarationChecker(MppCheckerKind.Common) {
        @OptIn(org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi::class)
        override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
          if (declaration is FirFile) {
            MacroCompiler.compileTransformCompilation(
              session = session,
              file = declaration,
              macro = macro,
              checkerContext = context,
              diagnosticsReporter = reporter
            )
          }
        }
      }
    )
  }
}
