/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_openda_costa_CtaStochObserver */

#ifndef _Included_org_openda_costa_CtaStochObserver
#define _Included_org_openda_costa_CtaStochObserver
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaCreateNative
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_openda_costa_CtaStochObserver_ctaCreateNative
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetStandardDeviation
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetStandardDeviation
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    getCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_openda_costa_CtaStochObserver_getCount
  (JNIEnv *, jobject);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetExpectations
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetExpectations
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetObservationDescriptions
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetObservationDescriptions
  (JNIEnv *, jobject);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetRealizations
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetRealizations
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetValues
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetValues
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaGetVariances
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaGetVariances
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaCreateSelection
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_openda_costa_CtaStochObserver_ctaCreateSelection
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaCreateTimeSelection
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_openda_costa_CtaStochObserver_ctaCreateTimeSelection
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_openda_costa_CtaStochObserver
 * Method:    ctaEvaluatePDF
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_openda_costa_CtaStochObserver_ctaEvaluatePDF
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif