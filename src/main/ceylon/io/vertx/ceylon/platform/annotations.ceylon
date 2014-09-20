import ceylon.language.meta.declaration { Module, ClassDeclaration }

"The annotation class for [[main]]."
shared final annotation class MainAnnotation(shared ClassDeclaration verticle)
    satisfies OptionalAnnotation<MainAnnotation, Module> {}

"."
shared annotation MainAnnotation main(ClassDeclaration verticle) => MainAnnotation(verticle);
