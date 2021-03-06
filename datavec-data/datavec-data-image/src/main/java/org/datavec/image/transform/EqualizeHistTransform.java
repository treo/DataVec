/*
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */
package org.datavec.image.transform;

import org.bytedeco.javacv.OpenCVFrameConverter;
import org.datavec.image.data.ImageWritable;

import java.util.Random;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.split;
import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * "<a href="https://opencv-srf.blogspot.com/2013/08/histogram-equalization.html">Histogram Equalization</a> equalizes the intensity distribution of an image or flattens the intensity distribution curve.
 * Used to improve the contrast of an image."
 *
 */
public class EqualizeHistTransform extends BaseImageTransform {

    int conversionCode;
    MatVector splitChannels = new MatVector();

    /**
     * Default transforms histogram equalization for CV_BGR2GRAY (grayscale)
     */

    public EqualizeHistTransform() {
        this(new Random(1234), CV_BGR2GRAY);
    }

    /**
     * Return contrast normalized object
     *
     * @param random Random
     * @param conversionCode  to transform,
     */
    public EqualizeHistTransform(Random random, int conversionCode) {
        super(random);
        this.conversionCode = conversionCode;
        converter = new OpenCVFrameConverter.ToMat();
    }

    /**
     * Takes an image and returns a transformed image.
     * Uses the random object in the case of random transformations.
     *
     * @param image  to transform, null == end of stream
     * @param random object to use (or null for deterministic)
     * @return transformed image
     */

    @Override
    public ImageWritable transform(ImageWritable image, Random random) {
        if (image == null) {
            return null;
        }
        Mat mat = (Mat) converter.convert(image.getFrame());
        Mat result = new Mat();
        try {
            if (conversionCode == CV_BGR2GRAY) {
                equalizeHist(mat, result);
            } else if (conversionCode == CV_BGR2YCrCb || conversionCode == COLOR_BGR2Luv) {
                split(mat, splitChannels);
                equalizeHist(splitChannels.get(0), splitChannels.get(0)); //equalize histogram on the 1st channel (Y)
                merge(splitChannels,result);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ImageWritable(converter.convert(result));
    }



}
