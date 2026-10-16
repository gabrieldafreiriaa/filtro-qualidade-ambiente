package br.gov.pr.celepar;

import boofcv.alg.misc.ImageStatistics;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

class BoofCVAvailabilityTest {

    @Test
    void testBoofCvClassesAreAvailable() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        GrayU8 gray = new GrayU8(10, 10);
        ConvertBufferedImage.convertFrom(img, gray, true);

        Assertions.assertNotNull(gray);
        Assertions.assertEquals(10, gray.getWidth());
        Assertions.assertEquals(10, gray.getHeight());

        double mean = ImageStatistics.mean(gray);
        Assertions.assertEquals(0.0, mean, 0.001);
    }
}
