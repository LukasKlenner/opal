package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.Alias;
import org.opalj.fpcf.properties.alias.MayAlias;

public class ShapeTest {

    public static void main(String[] args) {
        ShapeTest shapeTest = new ShapeTest();
        Shape a = shapeTest.addRectangle(new Circle(1.0));

        if (a != null) {
            System.out.println("circle added");
        }

        Shape b = shapeTest.addRectangle(new Rectangle(1.0, 2.0));

        if (b != null) {
            System.out.println("rectangle added");
        }
    }

    Shape[] shapes = new Shape[10];
    int index = 0;

    @Alias(mayAlias = {@MayAlias(reason = "", testClass = ShapeTest.class, id = "addRectangle")})
    public Shape addRectangle(
            @Alias(mayAlias = {@MayAlias(reason = "", testClass = ShapeTest.class, id = "addRectangle")})
            Shape shape) {

        if (shape instanceof Rectangle) {
            shapes[index++] = shape;
        } else {
            return null;
        }

        return shapes[index - 1];
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