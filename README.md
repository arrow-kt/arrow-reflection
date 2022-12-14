# Arrow Meta

Arrow Meta is a Kotlin library that allows you to write code that extends the capabilities of the Kotlin compiler. 
By using annotations in your code, you can instruct the compiler to perform certain actions, such as code generation, expression checking or AST transformations.

Arrow Meta provides a hierarchy of types that can be used to interact with the Kotlin compiler. At the top level of this hierarchy are the `Checker`, `Generate`, and `FrontendTransformer` types, which define the broad categories of supported compiler extensions.

To write a compiler plugin or macro with Arrow Meta, you would typically start by choosing one of these types to define the general category of transformation you want to perform. For example, if you want to perform static analysis or expression checking on your code to report errors and diagnostics, you would choose the `Checker` type. If you want to generate additional code based on the annotations in your code, you would choose the `Generate` type. If you want to manipulate the abstract syntax tree (AST) of your code, you would choose the `FrontendTransformer` type.

