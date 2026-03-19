/*
 * Copyright 2026 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.kotlin.internal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.kotlin.KotlinIsoVisitor;
import org.openrewrite.kotlin.tree.K;
import org.openrewrite.test.RewriteTest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.kotlin.Assertions.kotlin;

class PsiElementAssociationsTest implements RewriteTest {

    // ─────────────────────────────────────────────────────────────────────────
    // primitiveType()
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class PrimitiveType {

        @Test
        void intLiteral() {
            // PsiElementAssociations#primitiveType() → FirLiteralExpression path
            rewriteRun(
              kotlin(
                """
                  val x = 42
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.Literal visitLiteral(J.Literal literal, AtomicBoolean found) {
                            if (Integer.valueOf(42).equals(literal.getValue())) {
                                assertThat(literal.getType()).isEqualTo(JavaType.Primitive.Int);
                                found.set(true);
                            }
                            return super.visitLiteral(literal, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void longLiteral() {
            rewriteRun(
              kotlin(
                """
                  val x = 100L
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.Literal visitLiteral(J.Literal literal, AtomicBoolean found) {
                            if (Long.valueOf(100L).equals(literal.getValue())) {
                                assertThat(literal.getType()).isEqualTo(JavaType.Primitive.Long);
                                found.set(true);
                            }
                            return super.visitLiteral(literal, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void booleanLiteral() {
            rewriteRun(
              kotlin(
                """
                  val x = true
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.Literal visitLiteral(J.Literal literal, AtomicBoolean found) {
                            if (Boolean.TRUE.equals(literal.getValue())) {
                                assertThat(literal.getType()).isEqualTo(JavaType.Primitive.Boolean);
                                found.set(true);
                            }
                            return super.visitLiteral(literal, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void doubleLiteral() {
            rewriteRun(
              kotlin(
                """
                  val x = 3.14
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.Literal visitLiteral(J.Literal literal, AtomicBoolean found) {
                            if (Double.valueOf(3.14).equals(literal.getValue())) {
                                assertThat(literal.getType()).isEqualTo(JavaType.Primitive.Double);
                                found.set(true);
                            }
                            return super.visitLiteral(literal, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // variableType()
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class VariableType {

        @Test
        void localVal() {
            // PsiElementAssociations#variableType() → FirVariable path
            rewriteRun(
              kotlin(
                """
                  fun foo() {
                      val name: String = "hello"
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("name".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isNotNull();
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.String");
                                assertThat(variable.getVariableType()).isNotNull();
                                assertThat(variable.getVariableType().toString())
                                  .contains("name=name")
                                  .contains("type=kotlin.String");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void localVar() {
            rewriteRun(
              kotlin(
                """
                  fun foo() {
                      var count: Int = 0
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("count".equals(variable.getSimpleName())) {
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.Int");
                                assertThat(variable.getVariableType()).isNotNull();
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void resolvedNamedReference() {
            // PsiElementAssociations#variableType() → FirResolvedNamedReference path:
            // reading a field on another object resolves via a named reference.
            rewriteRun(
              kotlin(
                """
                  fun foo() {
                      val s = "hello"
                      val len = s.length
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("len".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isNotNull();
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // methodDeclarationType()
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class MethodDeclarationType {

        @Test
        void namedFunction() {
            // PsiElementAssociations#methodDeclarationType() → FirFunction path
            rewriteRun(
              kotlin(
                """
                  fun greet(name: String): String = "Hello, $name"
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodDeclaration visitMethodDeclaration(
                          J.MethodDeclaration method, AtomicBoolean found) {
                            if ("greet".equals(method.getSimpleName())) {
                                JavaType.Method mt = method.getMethodType();
                                assertThat(mt).isNotNull();
                                assertThat(mt.getName()).isEqualTo("greet");
                                assertThat(mt.getReturnType().toString()).isEqualTo("kotlin.String");
                                assertThat(mt.getParameterTypes()).hasSize(1);
                                assertThat(mt.getParameterTypes().getFirst().toString()).isEqualTo("kotlin.String");
                                found.set(true);
                            }
                            return super.visitMethodDeclaration(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void functionInsideClass() {
            rewriteRun(
              kotlin(
                """
                  class Calculator {
                      fun add(a: Int, b: Int): Int = a + b
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodDeclaration visitMethodDeclaration(
                          J.MethodDeclaration method, AtomicBoolean found) {
                            if ("add".equals(method.getSimpleName())) {
                                JavaType.Method mt = method.getMethodType();
                                assertThat(mt).isNotNull();
                                assertThat(mt.getReturnType().toString()).isEqualTo("kotlin.Int");
                                assertThat(mt.getParameterTypes()).hasSize(2);
                                assertThat(mt.getDeclaringType().getFullyQualifiedName()).isEqualTo("Calculator");
                                found.set(true);
                            }
                            return super.visitMethodDeclaration(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void enumEntryConstructor() {
            // PsiElementAssociations#methodDeclarationType() → FirEnumEntry path
            rewriteRun(
              kotlin(
                """
                  enum class Direction { NORTH, SOUTH, EAST, WEST }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.ClassDeclaration visitClassDeclaration(
                          J.ClassDeclaration classDecl, AtomicBoolean found) {
                            if ("Direction".equals(classDecl.getSimpleName())) {
                                JavaType.Class type = (JavaType.Class) classDecl.getType();
                                assertThat(type).isNotNull();
                                assertThat(type.getKind()).isEqualTo(JavaType.Class.Kind.Enum);
                                // Enum entries must appear as members
                                assertThat(type.getMembers())
                                  .extracting(JavaType.Variable::getName)
                                  .contains("NORTH", "SOUTH", "EAST", "WEST");
                                found.set(true);
                            }
                            return super.visitClassDeclaration(classDecl, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void anonymousFunction() {
            // PsiElementAssociations#methodDeclarationType() → FirAnonymousFunctionExpression path
            rewriteRun(
              kotlin(
                """
                  val transform: (Int) -> String = fun(x: Int): String = x.toString()
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("transform".equals(variable.getSimpleName())) {
                                // The variable must be typed as a Function1 (lambda / anon function)
                                assertThat(variable.getType()).isNotNull();
                                assertThat(variable.getType().toString()).contains("Function1");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // methodInvocationType()
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class MethodInvocationType {

        @Test
        void regularFunctionCall() {
            // PsiElementAssociations#methodInvocationType() → FirFunctionCall path
            rewriteRun(
              kotlin(
                """
                  fun bar(): Int = 1
                  fun foo() {
                      val r = bar()
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodInvocation visitMethodInvocation(
                          J.MethodInvocation method, AtomicBoolean found) {
                            if ("bar".equals(method.getSimpleName())) {
                                assertThat(method.getMethodType()).isNotNull();
                                assertThat(method.getMethodType().getName()).isEqualTo("bar");
                                assertThat(method.getMethodType().getReturnType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitMethodInvocation(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void safeCallMethodInvocation() {
            // PsiElementAssociations#methodInvocationType() → FirSafeCallExpression path
            rewriteRun(
              kotlin(
                """
                  fun foo(s: String?) {
                      val len = s?.length
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("len".equals(variable.getSimpleName())) {
                                // Safe-call on String? results in Int?
                                assertThat(variable.getType()).isNotNull();
                                // The resolved type for s?.length is kotlin.Int (nullable handled by K)
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void destructuringComponentCall() {
            // PsiElementAssociations#methodInvocationType() with KtDestructuringDeclarationEntry
            // → FirComponentCall path
            rewriteRun(
              kotlin(
                """
                  @Suppress("UNUSED_VARIABLE")
                  fun foo() {
                      val (a, b) = Pair(1, "hello")
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("a".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isNotNull();
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCallType() → ExpressionType
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class CallType {

        @Test
        void constructor() {
            // PsiElementAssociations#getCallType() → ExpressionType.CONSTRUCTOR
            rewriteRun(
              kotlin(
                """
                  class Foo(val x: Int)
                  val f = Foo(42)
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.NewClass visitNewClass(J.NewClass newClass, AtomicBoolean found) {
                            if (newClass.getClazz() instanceof J.Identifier id && "Foo".equals(id.getSimpleName())) {
                                assertThat(newClass.getConstructorType()).isNotNull();
                                assertThat(newClass.getConstructorType().getName()).isEqualTo("<constructor>");
                                assertThat(newClass.getClazz().getType().toString()).isEqualTo("Foo");
                                found.set(true);
                            }
                            return super.visitNewClass(newClass, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void methodInvocation() {
            // PsiElementAssociations#getCallType() → ExpressionType.METHOD_INVOCATION
            rewriteRun(
              kotlin(
                """
                  fun doubled(n: Int) = n * 2
                  val r = doubled(5)
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodInvocation visitMethodInvocation(
                          J.MethodInvocation method, AtomicBoolean found) {
                            if ("doubled".equals(method.getSimpleName())) {
                                assertThat(method.getMethodType()).isNotNull();
                                assertThat(method.getMethodType().getName()).isEqualTo("doubled");
                                assertThat(method.getMethodType().getReturnType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitMethodInvocation(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void qualifierExpression() {
            // PsiElementAssociations#getCallType() → ExpressionType.QUALIFIER
            // A field access on a companion object is resolved as a QUALIFIER by the FIR
            rewriteRun(
              kotlin(
                """
                  import kotlin.math.PI
                  val pi = PI
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("pi".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isNotNull();
                                assertThat(variable.getType().toString()).isEqualTo("kotlin.Double");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void anonymousFunctionSymbolSafeCall() {
            // PsiElementAssociations#getCallType() → FirSafeCallExpression branch
            // → selector FirFunctionCall resolved via FirAnonymousFunctionSymbol
            // → ExpressionType.METHOD_INVOCATION
            //
            // Scenario: nullable lambda variable safe-called via `?.invoke(...)`.
            // FIR represents `fn?.invoke(42)` as a FirSafeCallExpression whose inner
            // FirFunctionCall has a FirAnonymousFunctionSymbol as its callee symbol.
            rewriteRun(
              kotlin(
                """
                  @Suppress("UNUSED_VARIABLE")
                  fun test() {
                      val fn: ((Int) -> String)? = { it.toString() }
                      val result = fn?.invoke(42)
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodInvocation visitMethodInvocation(
                          J.MethodInvocation method, AtomicBoolean found) {
                            // fn?.invoke(42) surfaces as a J.MethodInvocation for "invoke"
                            if ("invoke".equals(method.getSimpleName())) {
                                assertThat(method.getMethodType()).isNotNull();
                                assertThat(method.getMethodType().getReturnType().toString())
                                  .isEqualTo("kotlin.String");
                                found.set(true);
                            }
                            return super.visitMethodInvocation(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // type() — parameterized / generic types
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class TypeResolution {

        @Test
        void parameterizedListType() {
            // PsiElementAssociations#type() with ConeClassLikeType + type arguments
            rewriteRun(
              kotlin(
                """
                  val names: List<String> = listOf("Alice", "Bob")
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("names".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isInstanceOf(JavaType.Parameterized.class);
                                JavaType.Parameterized param = (JavaType.Parameterized) variable.getType();
                                assertThat(param.getType().getFullyQualifiedName()).isEqualTo("kotlin.collections.List");
                                assertThat(param.getTypeParameters()).hasSize(1);
                                assertThat(param.getTypeParameters().getFirst().toString()).isEqualTo("kotlin.String");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void parameterizedMapType() {
            rewriteRun(
              kotlin(
                """
                  val m: Map<String, Int> = mapOf("a" to 1)
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.VariableDeclarations.NamedVariable visitVariable(
                          J.VariableDeclarations.NamedVariable variable, AtomicBoolean found) {
                            if ("m".equals(variable.getSimpleName())) {
                                assertThat(variable.getType()).isInstanceOf(JavaType.Parameterized.class);
                                JavaType.Parameterized param = (JavaType.Parameterized) variable.getType();
                                assertThat(param.getType().getFullyQualifiedName()).isEqualTo("kotlin.collections.Map");
                                assertThat(param.getTypeParameters()).hasSize(2);
                                assertThat(param.getTypeParameters().get(0).toString()).isEqualTo("kotlin.String");
                                assertThat(param.getTypeParameters().get(1).toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitVariable(variable, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void nullableType() {
            rewriteRun(
              kotlin(
                """
                  fun foo(s: String?): Int? = s?.length
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodDeclaration visitMethodDeclaration(
                          J.MethodDeclaration method, AtomicBoolean found) {
                            if ("foo".equals(method.getSimpleName())) {
                                assertThat(method.getMethodType()).isNotNull();
                                assertThat(method.getMethodType().getReturnType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitMethodDeclaration(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void genericTypeParameter() {
            rewriteRun(
              kotlin(
                """
                  fun <T> identity(t: T): T = t
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodDeclaration visitMethodDeclaration(
                          J.MethodDeclaration method, AtomicBoolean found) {
                            if ("identity".equals(method.getSimpleName())) {
                                JavaType.Method mt = method.getMethodType();
                                assertThat(mt).isNotNull();
                                // return type is a generic type variable T
                                assertThat(mt.getReturnType()).isInstanceOf(JavaType.GenericTypeVariable.class);
                                JavaType.GenericTypeVariable generic = (JavaType.GenericTypeVariable) mt.getReturnType();
                                assertThat(generic.getName()).isEqualTo("T");
                                found.set(true);
                            }
                            return super.visitMethodDeclaration(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void nestedClass() {
            rewriteRun(
              kotlin(
                """
                  class Outer {
                      inner class Inner
                      fun create(): Inner = Inner()
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodDeclaration visitMethodDeclaration(
                          J.MethodDeclaration method, AtomicBoolean found) {
                            if ("create".equals(method.getSimpleName())) {
                                assertThat(method.getMethodType().getReturnType()).isInstanceOf(JavaType.Class.class);
                                assertThat(method.getMethodType().getReturnType().toString()).isEqualTo("Outer$Inner");
                                found.set(true);
                            }
                            return super.visitMethodDeclaration(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // fir() / primary() – indirect coverage via edge-case PSI nodes
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class FirResolution {

        @Test
        void arrayAccessExpression() {
            // PsiElementAssociations#fir() → KtArrayAccessExpression branch.
            // Using MutableList whose `get(index)` is an explicit operator function,
            // so arr[i] unambiguously desugars to a J.MethodInvocation for `get`.
            rewriteRun(
              kotlin(
                """
                  fun foo() {
                      val list = mutableListOf(1, 2, 3)
                      val v = list[1]
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public J.MethodInvocation visitMethodInvocation(
                          J.MethodInvocation method, AtomicBoolean found) {
                            // list[1] desugars to list.get(1) via the operator convention
                            if ("<get>".equals(method.getSimpleName()) && method.getMethodType() != null
                                && "kotlin.collections.MutableList".equals(
                                    method.getMethodType().getDeclaringType().getFullyQualifiedName())) {
                                assertThat(method.getMethodType().getReturnType().toString()).isEqualTo("kotlin.Int");
                                found.set(true);
                            }
                            return super.visitMethodInvocation(method, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }

        @Test
        void prefixExpression() {
            // PsiElementAssociations#fir() → KtPrefixExpression branch
            rewriteRun(
              kotlin(
                """
                  fun foo() {
                      var n = 0
                      ++n
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicInteger unaryCount = new AtomicInteger(0);
                    new KotlinIsoVisitor<AtomicInteger>() {
                        @Override
                        public J.Unary visitUnary(J.Unary unary, AtomicInteger count) {
                            if (unary.getOperator() == J.Unary.Type.PreIncrement) {
                                assertThat(unary.getType()).isNotNull();
                                assertThat(unary.getType().toString()).isEqualTo("kotlin.Int");
                                count.incrementAndGet();
                            }
                            return super.visitUnary(unary, count);
                        }
                    }.visit(cu, unaryCount);
                    assertThat(unaryCount.get()).isGreaterThanOrEqualTo(1);
                })
              )
            );
        }

        @Test
        void whenExpression() {
            // PsiElementAssociations#fir() via K.When
            rewriteRun(
              kotlin(
                """
                  @Suppress("UNUSED_VARIABLE")
                  fun foo(n: Int): String {
                      return when (n) {
                          1    -> "one"
                          2    -> "two"
                          else -> "other"
                      }
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    new KotlinIsoVisitor<AtomicBoolean>() {
                        @Override
                        public K.When visitWhen(K.When when, AtomicBoolean found) {
                            assertThat(when.getType()).isNotNull();
                            assertThat(when.getType().toString()).isEqualTo("kotlin.String");
                            found.set(true);
                            return super.visitWhen(when, found);
                        }
                    }.visit(cu, found);
                    assertThat(found.get()).isTrue();
                })
              )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toString() – smoke-test that the internal map is populated
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    class Initialization {

        @Test
        void parsesWithoutErrors() {
            // Verifies that initialize() completes successfully and the parser
            // produces a well-formed tree (the underlying PsiElementAssociations
            // instance must have succeeded in building its elementMap).
            rewriteRun(
              kotlin(
                """
                  class Sample {
                      val value: Int = 42
                      fun display(): String = value.toString()
                  }
                  """
              )
            );
        }

        @Test
        void multipleClasses() {
            rewriteRun(
              kotlin(
                """
                  interface Printable {
                      fun print()
                  }
                  class Document(val title: String) : Printable {
                      override fun print() = println(title)
                  }
                  """,
                spec -> spec.afterRecipe(cu -> {
                    AtomicInteger classCount = new AtomicInteger(0);
                    new KotlinIsoVisitor<AtomicInteger>() {
                        @Override
                        public J.ClassDeclaration visitClassDeclaration(
                          J.ClassDeclaration classDecl, AtomicInteger count) {
                            count.incrementAndGet();
                            assertThat(classDecl.getType()).isNotNull();
                            return super.visitClassDeclaration(classDecl, count);
                        }
                    }.visit(cu, classCount);
                    assertThat(classCount.get()).isEqualTo(2);
                })
              )
            );
        }
    }
}
