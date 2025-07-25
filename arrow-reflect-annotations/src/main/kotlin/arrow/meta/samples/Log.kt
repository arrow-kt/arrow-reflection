package arrow.meta.samples

import kotlin.annotation.AnnotationTarget.*

@Target(
  CLASS,
  ANNOTATION_CLASS,
  TYPE_PARAMETER,
  PROPERTY,
  FIELD,
  LOCAL_VARIABLE,
  VALUE_PARAMETER,
  CONSTRUCTOR,
  FUNCTION,
  PROPERTY_GETTER,
  PROPERTY_SETTER,
  TYPE,
  EXPRESSION,
  FILE,
  TYPEALIAS
)
@Retention(AnnotationRetention.SOURCE)
annotation class Log
