/*
 * Copyright 2015 Effektif GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.effektif.workflow.api.workflow.diagram;

/**
 * A two-dimensional point on a BPMN diagram.
 */
public class Point {

  public double x;
  public double y;

  public Point() {
  }
  
  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Point x(double x) {
    this.x = x;
    return this;
  }
  
  public Point y(double y) {
    this.y = y;
    return this;
  }
  
  /**
   * Computes the distance to the given Point.
   * 
   * @param other - second Point
   * @throws IllegalArgumentException
   */
  public double distanceTo(Point other) {
    if (other == null) {
      throw new IllegalArgumentException("The second Point cannot be null.");
    }
    return distance(this.x, this.y, other.x, other.y);
  }
  
  public static Point of(double x, double y) {
    return new Point(x, y);
  }
  
  public static Point of(int x, int y) {
    return new Point(new Integer(x).doubleValue(), new Integer(y).doubleValue());
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Point other = (Point) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    return true;
  }
  
  /**
   * Computes the distance between two points using Pythagoras.
   *
   * @param x1 - x coordinate of the first point
   * @param y1 - y coordinate of the first point
   * @param x2 - x coordinate of the second point
   * @param y2 - y coordinate of the second point
   */
  private static double distance(double x1, double y1, double x2, double y2) {
    double distX = x1 - x2;
    double distY = y1 - y2;
    return Math.hypot(distX, distY);
  }
}
