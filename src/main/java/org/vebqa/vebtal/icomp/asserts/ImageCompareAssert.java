package org.vebqa.vebtal.icomp.asserts;

import org.assertj.core.api.AbstractAssert;
import org.vebqa.vebtal.icomp.ImageCompareResult;
import org.vebqa.vebtal.icomp.ImageCompareTest;

public class ImageCompareAssert extends AbstractAssert<ImageCompareAssert, String> {

    /**
     * A constructor to build our assertion class with the object we want make assertions on. 
     * @param anActualImageFile path to the actual file
     */
    public ImageCompareAssert(String anActualImageFile) {
        super(anActualImageFile, ImageCompareAssert.class);
    }

    /**
     * A fluent entry point to our specific assertion class, use it with static import. 
     * @param anActualImageFile path to the actual file
     * @return fluent API
     */
    public static ImageCompareAssert assertThat(String anActualImageFile) {
        return new ImageCompareAssert(anActualImageFile);
    }

    /**
     * A specific assertion 
     * @param anReferenceFile path to the reference file
     * @return fluent API
     */
    public ImageCompareAssert hasNoDifferencesTo(String anReferenceFile) {
        // check that actual ImageFilename we want to make assertions on is not null.
        isNotNull();

        // check condition
        ImageCompareTest compare = new ImageCompareTest(anReferenceFile, actual);
        ImageCompareResult result = null;

        try {
			result = compare.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (result.getResultType() != ImageCompareResult.CHECKOK) {
            if (result.getResultType() == ImageCompareResult.CURRENTNOTFOUND) {
                failWithMessage("Could not run image compare because current image not found: " + anReferenceFile);
            }
            if (result.getResultType() == ImageCompareResult.REFERENCENOTFOUND) {
                failWithMessage("Could not run image compare because reference image not found: " + actual);
            }
            if (result.getResultType() == ImageCompareResult.IMAGEDIMENSIONSDIFFER) {
                failWithMessage("Could not run image compare because image dimensions differ!");
            }
            if (result.getResultType() == ImageCompareResult.IMAGESDIFFER) {
                failWithMessage("Images have <%s> differences. Check difference image for more details: <%s>",
                                result.getDifferences(),                  
                                result.getResultFile());
            }

            failWithMessage("Expected result code to be <%s> but was <%s>", 0, result.getResultType());
        }

        // return the current assertion for method chaining
        return this;
    }
}