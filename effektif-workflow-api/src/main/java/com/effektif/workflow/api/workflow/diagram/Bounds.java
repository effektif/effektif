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
 * The two-dimensional bounding box of a BPMN diagram, or of a node on the diagram.
 */
public class Bounds {
  
  public Point lowerRight;
  public Point upperLeft;

  public Bounds() {
  }
  
  public Bounds(Point upperLeft, Point lowerRight) {
    this.upperLeft = upperLeft;
    this.lowerRight = lowerRight;
  }
  
  public Bounds(double ulx, double uly, double lrx, double lry) {
    this.upperLeft = new Point(ulx, uly);
    this.lowerRight = new Point(lrx, lry);
  }

  public Bounds(Point upperLeft, double width, double height) {
    this.upperLeft = upperLeft;
    this.lowerRight = upperLeft.translate(width, height);
  }

  public Bounds lowerRight(Point lowerRight) {
    this.lowerRight = lowerRight;
    return this;
  }


  public Bounds upperLeft(Point upperLeft) {
    this.upperLeft = upperLeft;
    return this;
  }

  public double getWidth() {
    if (upperLeft == null || lowerRight == null) {
      return 0;
    }
    return Math.abs(upperLeft.x - lowerRight.x);
  }

  public double getHeight() {
    if (upperLeft == null || lowerRight == null) {
      return 0;
    }
    return Math.abs(upperLeft.y - lowerRight.y);
  }

  /**
   * Validates this Bounds and returns true if the distance between the two points is bigger than 0.0 
   * and at least one of the x and y values is different than 0.0.
   */
  public boolean isValid() {
    // upperLeft and lowerRight must exist
    if (upperLeft == null || lowerRight == null) {
      return false;
    }
    // either of the x values must be different than 0.0
    if (upperLeft.x == 0.0 && lowerRight.x == 0.0) {
      return false;
    }
    // either of the y values must be different than 0.0
    if (upperLeft.y == 0.0 && lowerRight.y == 0.0) {
      return false;
    }
    // the distance between the two points must be bigger than 0.0
    if (upperLeft.distanceTo(lowerRight) == 0.0) {
      return false;
    }
    return true;
  }
  
  public static Bounds of(double ulx, double uly, double lrx, double lry) {
    return new Bounds(ulx, uly, lrx, lry);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((lowerRight == null) ? 0 : lowerRight.hashCode());
    result = prime * result + ((upperLeft == null) ? 0 : upperLeft.hashCode());
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
    Bounds other = (Bounds) obj;
    if (lowerRight == null) {
      if (other.lowerRight != null)
        return false;
    } else if (!lowerRight.equals(other.lowerRight))
      return false;
    if (upperLeft == null) {
      if (other.upperLeft != null)
        return false;
    } else if (!upperLeft.equals(other.upperLeft))
      return false;
    return true;
  }
  
}
