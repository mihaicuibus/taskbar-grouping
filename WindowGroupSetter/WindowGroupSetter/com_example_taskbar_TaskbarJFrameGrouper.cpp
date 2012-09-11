#include "com_example_taskbar_TaskbarJFrameGrouper.h"

#include <shlobj.h>
#include <propkey.h>
#include <propvarutil.h>

void SetPropertyStore(HWND hwnd, LPCWSTR szValue) {
	IPropertyStore *pps;
	HRESULT hr = SHGetPropertyStoreForWindow(hwnd, IID_IPropertyStore, (void **)&pps);

	if (SUCCEEDED(hr)) {
		PROPVARIANT var;
		HRESULT hr = InitPropVariantFromString(szValue, &var);

		if (SUCCEEDED(hr)) {
			hr = pps->SetValue(PKEY_AppUserModel_ID, var);
			PropVariantClear(&var);
		}

		pps->Release();
	}
}

JNIEXPORT void JNICALL Java_com_example_taskbar_TaskbarJFrameGrouper_assignWindowToGroup(JNIEnv *env, jobject obj, jlong hwnd, jstring group) {
	const jchar *nativeString = env->GetStringChars(group, false);
	SetPropertyStore((HWND) hwnd, (LPCWSTR) nativeString);
	env->ReleaseStringChars(group, nativeString);
}