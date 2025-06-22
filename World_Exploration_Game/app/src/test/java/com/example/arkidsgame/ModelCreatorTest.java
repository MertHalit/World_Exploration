package com.example.arkidsgame;

import android.content.Context;

import com.example.arkidsgame.ar.ModelCreator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ModelCreatorTest {

    @Mock
    Context mockContext;

    private ModelCreator modelCreator;

    @Before
    public void setup() {
        modelCreator = new ModelCreator(mockContext);
    }

    @Test
    public void createHairModel_shouldReturnTrue() {
        // Test saç modeli oluşturma
        assertTrue(modelCreator.createHairModel(0));
        assertTrue(modelCreator.createHairModel(1));
        assertTrue(modelCreator.createHairModel(2));
    }

    @Test
    public void createEyeModel_shouldReturnTrue() {
        // Test göz modeli oluşturma
        assertTrue(modelCreator.createEyeModel(0));
        assertTrue(modelCreator.createEyeModel(1));
        assertTrue(modelCreator.createEyeModel(2));
    }

    @Test
    public void createAccessoryModel_shouldReturnTrue() {
        // Test aksesuar modeli oluşturma
        assertTrue(modelCreator.createAccessoryModel(0));
        assertTrue(modelCreator.createAccessoryModel(1));
        assertTrue(modelCreator.createAccessoryModel(2));
    }
} 