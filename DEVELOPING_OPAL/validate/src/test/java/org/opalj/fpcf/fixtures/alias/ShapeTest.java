package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.Alias;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.MustAlias;
import org.opalj.fpcf.properties.alias.NoAlias;

public class ShapeTest {

    public static void main(String[] args) {
        ShapeTest shapeTest = new ShapeTest();
        Shape a = shapeTest.addRectangle(createCircle(1.0));

        if (a != null) {
            System.out.println("circle added");
        }

        Shape b = shapeTest.addRectangle(createRectangle(1.0, 1.0));

        if (b != null) {
            System.out.println("rectangle added");
        }

        Shape c = shapeTest.setCurrentRectangle(createSquare(1.0));

        if (c != null) {
            System.out.println("square set");
        }

        Shape d = createShapeNullPossible("circle", 2.0);

        if (d != null) {
            System.out.println("circle created");
        }

        Shape e = createShapeNullImpossible("circle", 2.0);

        if (e != null) {
            System.out.println("circle created");
        }

        Shape nullShape = createNull();

        if (nullShape == null) {
            System.out.println("created null");
        }

        shapeTest.getLastOr(new Square(1.0));
        shapeTest.getLastOrCircle(new Square(1.0));
    }

    Shape[] shapes = new Shape[10];
    int index = 0;

    Shape currentShape;

//    @Alias(mayAlias = {@MayAlias(reason = "", testClass = ShapeTest.class, id = "addRectangle")})
    public Shape addRectangle(
//            @Alias(mayAlias = {@MayAlias(reason = "", testClass = ShapeTest.class, id = "addRectangle")})
            Shape shape) {

        if (shape instanceof Rectangle) {
            shapes[index++] = shape;
            return shapes[index - 1];
        } else {
            return null;
        }

    }

    @Alias(mayAlias = {@MayAlias(reason = "return value might be param", testClass = ShapeTest.class, id = "getLastOrParam"),
                       @MayAlias(reason = "return value might be null", testClass = ShapeTest.class, id = "getLastOrNull", aliasWithNull = true)})
    public Shape getLastOr(
            @Alias(mayAlias = {@MayAlias(reason = "return value might be param", testClass = ShapeTest.class, id = "getLastOrParam")})
            Shape shape) {

        Shape currentShape = shape;

        for (int i = 0; i < shapes.length; i++) {

            Shape next = shapes[i];
            if (next != null) {
                currentShape = next;
            }
        }

        return currentShape;
    }

    @Alias(mayAlias = {@MayAlias(reason = "return value might be param", testClass = ShapeTest.class, id = "getLastOrCircleParam"),
            @MayAlias(reason = "return value might be new circle", testClass = ShapeTest.class, id = "getLastOrCircleNewCircle"),
            @MayAlias(reason = "return value might be null", testClass = ShapeTest.class, id = "getLastOrNull", aliasWithNull = true)})
    public Shape getLastOrCircle(
            @Alias(mayAlias = {@MayAlias(reason = "return value might be param", testClass = ShapeTest.class, id = "getLastOrCircleParam")}
                   //noAlias = {@NoAlias(reason = "param new circle", testClass = ShapeTest.class, id = "getLastOrCircleParamNewCircle")}
            )
            Shape shape) {

        Shape currentShape = shape;

        Shape circle = new @Alias(mayAlias = {@MayAlias(reason = "return value might be new circle", testClass = ShapeTest.class, id = "getLastOrCircleNewCircle")}
                                 // noAlias = {@NoAlias(reason = "param new circle", testClass = ShapeTest.class, id = "getLastOrCircleParamNewCircle")}
        )
                Circle(2.0);

        for (int i = 0; i < shapes.length; i++) {

            Shape next = shapes[i];
            if (next != null) {
                currentShape = next;
            }
        }

        if (!(currentShape instanceof Circle)) {
            currentShape = circle;
        }

        return currentShape;
    }

    @Alias(mayAlias = {@MayAlias(reason = "return value may be parameter", testClass = ShapeTest.class, id = "setCurrentRectangle"),
                       @MayAlias(reason = "return value might be null", testClass = ShapeTest.class, id = "setCurrentRectangleNull", aliasWithNull = true)})
    public Shape setCurrentRectangle(
            @Alias(mayAlias = {@MayAlias(reason = "return value may be parameter", testClass = ShapeTest.class, id = "setCurrentRectangle")})
            Shape shape) {
        if (shape instanceof Rectangle) {
            currentShape = shape;
            return shape;
        } else {
            return null;
        }

    }

    @Alias(noAlias = {@NoAlias(reason = "Method never returns null", testClass = ShapeTest.class, id = "createCircleNull", aliasWithNull = true)})
    public static Circle createCircle(double radius) {
        return new Circle(radius);
    }

    public static Rectangle createRectangle(double width, double height) {
        return new Rectangle(width, height);
    }

    public static Square createSquare(double side) {
        return new Square(side);
    }

    @Alias(mustAlias = {@MustAlias(reason = "Method always returns null", testClass = ShapeTest.class, id = "createNull", aliasWithNull = true)})
    public static Shape createNull() {
        return null;
    }

    @Alias(mayAlias = {@MayAlias(reason = "Method may return null", testClass = ShapeTest.class, id = "createCircleNull", aliasWithNull = true)})
    public static Shape createShapeNullPossible(String name, double length) {
        switch (name) {
            case "circle":
                return new Circle(length);
            case "rectangle":
                return new Rectangle(length, length);
            case "square":
                return new Square(length);
            default:
                return null;
        }
    }

    @Alias(noAlias = {@NoAlias(reason = "Method never returns null", testClass = ShapeTest.class, id = "createCircleNull", aliasWithNull = true)})
    public static Shape createShapeNullImpossible(String name, double length) {
        switch (name) {
            case "circle":
                return new Circle(length);
            case "rectangle":
                return new Rectangle(length, length);
            default:
                return new Square(length);
        }
    }

}

class Shape {

    public double area() {
        return 0.0;
    }

}

class Circle extends Shape {

    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

}

class Rectangle extends Shape {

    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }

}

class Square extends Rectangle {

    public Square(double side) {
        super(side, side);
    }

}