package greenhouse

import "testing"

func TestStableIdentityAndCompleteness(t *testing.T) {
	result, err := Parse([]byte(`{"meta":{"total":1},"jobs":[{"id":123,"title":"Engineer","absolute_url":"https://example.com/jobs?utm_source=a"}]}`))
	if err != nil || !result.Verified || result.Jobs[0].ID != "123" {
		t.Fatalf("unexpected result: %+v %v", result, err)
	}
	for _, body := range []string{`{"jobs":[{"id":123,"absolute_url":"https://example.com"}]}`, `{"meta":{"total":2},"jobs":[{"id":123,"absolute_url":"https://example.com"}]}`} {
		r, e := Parse([]byte(body))
		if e != nil || r.Verified {
			t.Fatalf("unverified inventory accepted: %+v %v", r, e)
		}
	}
	if _, err := Parse([]byte(`{"jobs":[{"absolute_url":"https://example.com"}]}`)); err == nil {
		t.Fatal("missing provider ID accepted")
	}
}
