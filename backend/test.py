import os
import textwrap
import warnings
from flask import Flask, request, jsonify
from dotenv import load_dotenv

from langchain_community.document_loaders import PyPDFLoader
from langchain_community.vectorstores import Chroma
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_openai import ChatOpenAI
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.chains import create_retrieval_chain

# === Configuration ===
load_dotenv()

PDF_DIRECTORY = r"D:\FYP\FYP\pdf_books"
GOOGLE_CREDENTIAL_PATH = r"D:\FYP\FYP\pdf_books\gen-lang.json"
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = GOOGLE_CREDENTIAL_PATH

# === Initialize Flask app ===
app = Flask(__name__)

# === Globals ===
vectorstore = None
rag_chain = None
llm_primary = None
llm_fallback = None

def initialize_system():
    global vectorstore, rag_chain, llm_primary, llm_fallback

    print("📚 Loading PDF documents...")
    all_docs = []
    for filename in os.listdir(PDF_DIRECTORY):
        if filename.endswith(".pdf"):
            loader = PyPDFLoader(os.path.join(PDF_DIRECTORY, filename))
            all_docs.extend(loader.load())

    print("✂️ Splitting documents into chunks...")
    splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
    docs = splitter.split_documents(all_docs)

    print("🔎 Creating Chroma vectorstore...")
    embeddings = GoogleGenerativeAIEmbeddings(model="models/text-embedding-004")
    vectorstore = Chroma.from_documents(documents=docs, embedding=embeddings, persist_directory="db")
    vectorstore.persist()

    retriever = vectorstore.as_retriever(search_type="similarity", search_kwargs={"k": 3})

    print("🧠 Initializing LLMs...")
    llm_primary = ChatGoogleGenerativeAI(model="gemini-1.5-flash", temperature=0.1)
    llm_fallback = ChatOpenAI(model="gpt-3.5-turbo", temperature=0.7, openai_api_key=os.getenv("openai_api_key"))

    print("🔗 Creating RAG chain without memory...")
    prompt = ChatPromptTemplate.from_messages([
        ("system", "You are a helpful and child-friendly educational assistant. Use the context to answer simply:\n\n{context}"),
        ("human", "{input}")
    ])

    qa_chain = create_stuff_documents_chain(llm=llm_primary, prompt=prompt)
    rag_chain = create_retrieval_chain(retriever, qa_chain)

def get_response(user_input: str) -> str:
    try:
        result = rag_chain.invoke({"input": user_input})
        answer = result.get("answer", "").strip().replace("*", "")

        if not answer or any(x in answer.lower() for x in ["i don't know", "not sure", "no answer"]):
            print("⚠️ Gemini fallback triggered, using GPT...")
            retrieved_docs = vectorstore.as_retriever().invoke(user_input)
            context = "\n".join([doc.page_content for doc in retrieved_docs])
            fallback_response = llm_fallback.invoke(f"Q: {user_input}\nContext:\n{context}")
            return textwrap.fill(str(fallback_response), width=80)

        return textwrap.fill(answer, width=80)
    except Exception as e:
        print(f"❌ Error during response: {e}")
        return "Sorry, something went wrong. Please try again."

@app.route("/ask", methods=["POST"])
def ask():
    data = request.get_json()
    if "question" not in data:
        return jsonify({"error": "Missing 'question' field"}), 400

    question = data["question"]
    answer = get_response(question)
    return jsonify({"answer": answer})

if __name__ == "__main__":
    warnings.filterwarnings("ignore")
    initialize_system()
    app.run(host="0.0.0.0", port=5000, debug=True, threaded=True)
