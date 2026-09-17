package org.openmrs.module.attachments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.openmrs.ConceptComplex;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsConstants.ContentFamily;
import org.openmrs.module.attachments.obs.AttachmentComplexData;
import org.openmrs.module.attachments.obs.ComplexDataHelper;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.obs.ComplexData;
import org.springframework.mock.web.MockMultipartFile;

public class ComplexObsSaverTest {

	@Test
	public void saveImageAttachment_shouldNotMixUpConcurrentUploads() throws Exception {
		ComplexObsSaver saver = new ComplexObsSaver();
		saver.context = mock(AttachmentsContext.class);
		saver.complexDataHelper = mock(ComplexDataHelper.class);
		ObsService obsService = mock(ObsService.class);

		when(saver.context.getObsService()).thenReturn(obsService);
		when(saver.context.getMaxStorageFileSize()).thenReturn(5.0);
		when(saver.context.getConceptComplex(ContentFamily.IMAGE)).thenReturn(new ConceptComplex());
		when(obsService.saveObs(any(Obs.class), anyString())).thenAnswer(i -> i.getArgument(0));

		CyclicBarrier bothPrepared = new CyclicBarrier(2);
		when(saver.complexDataHelper.build(anyString(), anyString(), any(), anyString())).thenAnswer(i -> {
			bothPrepared.await();
			AttachmentComplexData data = mock(AttachmentComplexData.class);
			when(data.asComplexData()).thenReturn(new ComplexData(i.getArgument(1), i.getArgument(2)));
			return data;
		});

		ExecutorService executor = Executors.newFixedThreadPool(2);
		Future<Obs> first = executor.submit(() -> save(saver, "first.png", "First caption"));
		Future<Obs> second = executor.submit(() -> save(saver, "second.png", "Second caption"));
		Obs firstObs = first.get();
		Obs secondObs = second.get();
		executor.shutdown();

		assertNotSame(firstObs, secondObs);
		assertEquals("First caption", firstObs.getComment());
		assertEquals("first.png", firstObs.getComplexData().getTitle());
		assertEquals("Second caption", secondObs.getComment());
		assertEquals("second.png", secondObs.getComplexData().getTitle());
	}

	private Obs save(ComplexObsSaver saver, String fileName, String caption) throws Exception {
		return saver.saveImageAttachment(null, new Patient(), null, caption,
				new MockMultipartFile(fileName, fileName, "image/png", fileName.getBytes()),
				ValueComplex.INSTRUCTIONS_DEFAULT, null, null);
	}
}