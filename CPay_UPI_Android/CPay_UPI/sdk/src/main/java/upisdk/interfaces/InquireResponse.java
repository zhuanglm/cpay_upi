package upisdk.interfaces;

public interface InquireResponse<CPayUPIInquireResult>
{
    void gotInquireResult(CPayUPIInquireResult object);
}